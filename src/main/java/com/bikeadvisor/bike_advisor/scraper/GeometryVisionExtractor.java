package com.bikeadvisor.bike_advisor.scraper;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GeometryVisionExtractor {

    private static final Logger log = LoggerFactory.getLogger(GeometryVisionExtractor.class);
    private static final int MAX_DIMENSION = 1568;

    private static final String SYSTEM_PROMPT = """
            You are a bike geometry data extractor. Given an image of a bike geometry table,
            extract all geometry values and return them as a JSON array — one object per size.

            Each object must use exactly these keys (use null if a value is not present in the table):
            - sizeLabel   (string: the size label, e.g. "44", "S", "M")
            - reach
            - stack
            - topTubeEffective   (top tube horizontal or effective length)
            - headTubeAngle
            - seatTubeAngleEffective   (effective seat tube angle)
            - headTubeLength
            - wheelbase
            - chainstay
            - bbDrop   (bottom bracket drop)
            - forkOffset   (fork rake/offset)
            - trail
            - seatTubeLength
            - standover

            Rules:
            - Return ONLY the JSON array, no explanation or markdown code fences.
            - All numeric values should be plain numbers (no units, no degree symbols).
            - All length values must be in mm. If the table uses cm, multiply by 10 before returning.
            """;

    private final AnthropicClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeometryVisionExtractor() {
        // Reads ANTHROPIC_API_KEY from environment
        this.client = AnthropicOkHttpClient.fromEnv();
    }

    public List<BikeGeometry> extractGeometry(String imageUrl, String geometryKey) throws Exception {
        log.info("Downloading geometry image: {}", imageUrl);
        String base64Image = downloadAndResizeImage(imageUrl);

        List<ContentBlockParam> content = List.of(
                ContentBlockParam.ofImage(
                        ImageBlockParam.builder()
                                .source(Base64ImageSource.builder()
                                        .data(base64Image)
                                        .mediaType(Base64ImageSource.MediaType.IMAGE_PNG)
                                        .build())
                                .build()
                ),
                ContentBlockParam.ofText(
                        TextBlockParam.builder()
                                .text("Extract the geometry table from this image.")
                                .build()
                )
        );

        Instant start = Instant.now();
        log.info("Sending image to Claude Vision API");

        Message response = client.messages().create(
                MessageCreateParams.builder()
                        .model("claude-opus-4-6")
                        .maxTokens(4096L)
                        .system(SYSTEM_PROMPT)
                        .addUserMessageOfBlockParams(content)
                        .build()
        );

        Instant end = Instant.now();
        log.info("Claude Vision response received in {}ms, stop_reason={}",
                end.toEpochMilli() - start.toEpochMilli(), response.stopReason());

        String json = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .findFirst()
                .orElseThrow(() -> new Exception("No text response from Claude"));

        // Strip markdown code fences if Claude includes them despite instructions
        json = json.strip();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
        }

        return parseJsonToGeometries(json, geometryKey);
    }

    private String downloadAndResizeImage(String imageUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        BufferedImage image;
        try (InputStream in = conn.getInputStream()) {
            image = ImageIO.read(in);
        }
        if (image == null) throw new Exception("Cannot decode image from: " + imageUrl);

        int width = image.getWidth();
        int height = image.getHeight();
        int maxDim = Math.max(width, height);

        if (maxDim > MAX_DIMENSION) {
            double scale = (double) MAX_DIMENSION / maxDim;
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, newWidth, newHeight);
            g.drawImage(image, 0, 0, newWidth, newHeight, null);
            g.dispose();
            log.debug("Resized image {}x{} → {}x{}", width, height, newWidth, newHeight);
            image = resized;
        } else {
            // Flatten transparency to white even if no resize needed
            BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = rgb;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private List<BikeGeometry> parseJsonToGeometries(String json, String geometryKey) throws Exception {
        JsonNode array = objectMapper.readTree(json);
        if (!array.isArray()) throw new Exception("Expected JSON array from Claude, got: " + json);

        List<BikeGeometry> geometries = new ArrayList<>();
        for (JsonNode node : array) {
            BikeGeometry geo = new BikeGeometry();
            geo.setBikeGeometryKey(geometryKey);
            geo.setSizeLabel(textOrNull(node, "sizeLabel"));
            geo.setReach(doubleOrNull(node, "reach"));
            geo.setStack(doubleOrNull(node, "stack"));
            geo.setTopTubeEffective(doubleOrNull(node, "topTubeEffective"));
            geo.setHeadTubeAngle(doubleOrNull(node, "headTubeAngle"));
            geo.setSeatTubeAngleEffective(doubleOrNull(node, "seatTubeAngleEffective"));
            geo.setHeadTubeLength(doubleOrNull(node, "headTubeLength"));
            geo.setWheelbase(doubleOrNull(node, "wheelbase"));
            geo.setChainstay(doubleOrNull(node, "chainstay"));
            geo.setBbDrop(doubleOrNull(node, "bbDrop"));
            geo.setForkOffset(doubleOrNull(node, "forkOffset"));
            geo.setTrail(doubleOrNull(node, "trail"));
            geo.setSeatTubeLength(doubleOrNull(node, "seatTubeLength"));
            geo.setStandover(doubleOrNull(node, "standover"));
            geometries.add(geo);
        }
        return geometries;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    private Double doubleOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asDouble();
    }
}
