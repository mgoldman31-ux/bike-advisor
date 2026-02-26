package com.bikeadvisor.bike_advisor.scraper.brands.specialized;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Brand;
import com.bikeadvisor.bike_advisor.model.Discipline;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpecializedParser {

    private static final Logger log = LoggerFactory.getLogger(SpecializedParser.class);

    private static final String BASE_URL = "https://www.specialized.com";

    /**
     * Parses the Specialized road bikes product grid.
     * Uses data-component="product-tile" and semantic elements only —
     * styled-component class names (sc-*) are hashed and change on deploy.
     */
    public static List<BikeSummary> parseBikeSummaryDoc(Document doc) {
        List<BikeSummary> results = new ArrayList<>();

        Elements tiles = doc.select("article[data-component=product-tile]");

        for (Element tile : tiles) {
            // The link wrapping the h2 is the most reliable source for both name and URL
            Element nameLink = tile.selectFirst("a:has(h2)");
            if (nameLink == null) continue;

            String name = nameLink.selectFirst("h2").text().trim();
            if (name.isEmpty()) continue;
            if (name.contains("Frameset")) continue;

            String href = nameLink.attr("href");

            // Normalize to /us/en/<slug>/p/<id> — some hrefs omit the locale prefix
            String normalizedHref = href.replaceAll("\\?.*$", ""); // strip color query param
            if (!normalizedHref.startsWith("/us/en")) {
                normalizedHref = "/us/en" + normalizedHref;
            }
            String productUrl = BASE_URL + normalizedHref;

            // Geometry key: the slug portion before /p/<id>
            String geometryKey = normalizedHref.replaceAll("/p/.*$", "").replaceAll("^.*/", "");

            // Price is in a <p> directly containing "$"
            String priceText = null;
            Element priceEl = tile.selectFirst("p:containsOwn($)");
            if (priceEl != null) priceText = priceEl.text();

            BikeSummary bike = new BikeSummary();
            bike.setBrand(Brand.SPECIALIZED.getBrandName());
            bike.setModel(name);
            bike.setProductUrl(productUrl);
            bike.setPrice(BikeSummary.parsePrice(priceText));
            bike.setGeometryKey(geometryKey);
            bike.setDiscipline(Discipline.ROAD);

            results.add(bike);
        }

        return results;
    }

    /**
     * Parses the geometry table from a Specialized product page.
     * The table lives inside a section with id="geometry".
     * Values are plain td text like "522mm" or "71.4°" — no nested spans.
     * Returns one BikeGeometry per size column.
     */
    public static List<BikeGeometry> parseGeometryTableDoc(Document doc, String geometryKey) {
        // Find table via the stable id="geometry" section; fall back to the h3 heading
        Element table = null;
        Element geoSection = doc.selectFirst("[id=geometry]");
        if (geoSection != null) {
            table = geoSection.selectFirst("table");
        }
        if (table == null) {
            Element heading = doc.selectFirst("h3:containsOwn(Geometry)");
            if (heading != null) {
                Element ancestor = heading.parent();
                while (ancestor != null && table == null) {
                    table = ancestor.selectFirst("table");
                    ancestor = ancestor.parent();
                }
            }
        }
        if (table == null) {
            log.warn("Geometry table not found for key='{}' — page structure may have changed", geometryKey);
            return List.of();
        }

        // Size labels: header row th elements, skipping the first (blank label column)
        Elements headerCells = table.select("thead tr th");
        List<String> sizes = new ArrayList<>();
        for (int i = 1; i < headerCells.size(); i++) {
            sizes.add(headerCells.get(i).text().trim());
        }
        if (sizes.isEmpty()) return List.of();

        List<BikeGeometry> geometries = new ArrayList<>();
        for (String size : sizes) {
            BikeGeometry geo = new BikeGeometry();
            geo.setBikeGeometryKey(geometryKey);
            geo.setSizeLabel(size);
            geometries.add(geo);
        }

        for (Element row : table.select("tbody tr")) {
            Elements cells = row.select("td");
            if (cells.size() < 2) continue;
            String label = cells.first().text().trim().toLowerCase().replace("-", " ");

            // Wheel size is a string (e.g. "700c"), handle before numeric parsing
            if (label.contains("wheel size")) {
                String wheelSizeText = cells.get(1).text().trim();
                if (!wheelSizeText.isEmpty()) {
                    geometries.forEach(g -> g.setWheelSize(wheelSizeText));
                }
                continue;
            }

            for (int i = 1; i < Math.min(cells.size(), sizes.size() + 1); i++) {
                Double value = parseDouble(cells.get(i).text());
                applyGeometryValue(geometries.get(i - 1), label, value);
            }
        }

        return geometries;
    }

    private static void applyGeometryValue(BikeGeometry geo, String label, Double value) {
        if (label.contains("seat tube length"))                         geo.setSeatTubeLength(value);
        else if (label.contains("seat tube angle"))                     geo.setSeatTubeAngleEffective(value);
        else if (label.contains("head tube length"))                    geo.setHeadTubeLength(value);
        else if (label.contains("head tube angle"))                     geo.setHeadTubeAngle(value);
        else if (label.contains("top tube"))                            geo.setTopTubeEffective(value);
        else if (label.contains("fork rake") || label.contains("fork offset")) geo.setForkOffset(value);
        else if (label.contains("trail"))                               geo.setTrail(value);
        else if (label.contains("wheelbase"))                           geo.setWheelbase(value);
        else if (label.contains("chainstay") || label.contains("chain stay")) geo.setChainstay(value);
        else if (label.contains("bb drop") || label.contains("b b drop"))   geo.setBbDrop(value);
        else if (label.contains("stack"))                               geo.setStack(value);
        else if (label.contains("reach"))                               geo.setReach(value);
        else if (label.contains("standover") || label.contains("stand over")) geo.setStandover(value);
        // BB Height, Front Center, Fork Length, Crank/Handlebar/Stem/Saddle/Seatpost — not in BikeGeometry
    }

    private static Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim().replaceAll("[^\\d.,-]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
