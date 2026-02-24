package com.bikeadvisor.bike_advisor.scraper.brands.giant;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiantParser {

    private static final Logger log = LoggerFactory.getLogger(GiantParser.class);

    private static final String BASE_URL = "https://www.giant-bicycles.com";
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(20\\d{2})\\b");

    /**
     * Parses a Giant bike listing page.
     * When a tile links to a series ("View 2 Models"), fetcher is called on that URL
     * and individual models are extracted from the series compare table.
     */
    public static List<BikeSummary> parseBikeSummaryDoc(Document doc, Function<String, Document> fetcher) {
        return parseBikeSummaryDoc(doc, fetcher, new HashSet<>());
    }

    private static List<BikeSummary> parseBikeSummaryDoc(Document doc, Function<String, Document> fetcher, Set<String> visited) {
        List<BikeSummary> results = new ArrayList<>();

        Elements tiles = doc.select("div.tile.bike-summary");

        for (Element tile : tiles) {
            Element link = tile.selectFirst("a[data-product-name]");
            if (link == null) continue;

            String productName = link.attr("data-product-name");
            if (productName.contains("Frameset")) continue;
            if (productName.isEmpty()) continue;

            String href = link.attr("href");
            String productUrl = BASE_URL + href;
            String classAttr = tile.className();

            Integer modelYear = extractYear(classAttr);
            Discipline discipline = inferDiscipline(classAttr);

            // "View X Models" tile — expand to individual models via the series compare table
            if (tile.selectFirst("span.modelslabel") != null) {
                if (!visited.contains(productUrl)) {
                    visited.add(productUrl);
                    log.info("Following series page for '{}': {}", productName, productUrl);
                    Document seriesDoc = fetcher.apply(productUrl);
                    if (seriesDoc != null) {
                        results.addAll(parseSeriesPage(seriesDoc, modelYear, discipline));
                    }
                }
                continue;
            }

            String geometryKey = href.replaceFirst("^/[^/]+/bikes-", "");

            String priceText = null;
            Element priceEl = tile.selectFirst(".prices .price");
            if (priceEl != null) priceText = priceEl.text();

            BikeSummary bike = new BikeSummary();
            bike.setBrand(Brand.GIANT.getBrandName());
            bike.setModel(productName);
            bike.setModelYear(modelYear);
            bike.setProductUrl(productUrl);
            bike.setPriceText(priceText);
            bike.setGeometryKey(geometryKey);
            bike.setDiscipline(discipline);

            results.add(bike);
        }

        return results;
    }

    /**
     * Parses individual models from a series page's compare table.
     * Year and discipline are inherited from the series tile on the listing page,
     * since the compare table does not carry those class-based attributes.
     */
    private static List<BikeSummary> parseSeriesPage(Document doc, Integer modelYear, Discipline discipline) {
        List<BikeSummary> results = new ArrayList<>();

        Elements topParts = doc.select("table.serie-compare-table td.top-part");

        for (Element td : topParts) {
            // Model name and URL — prefer the link inside div.h3, fall back to any link in the td
            Element h3Link = td.selectFirst("div.h3 a[href]");
            Element anyLink = h3Link != null ? h3Link : td.selectFirst("a[href]");
            if (anyLink == null) continue;

            String modelName = td.selectFirst("div.h3") != null
                    ? td.selectFirst("div.h3").text().trim()
                    : anyLink.text().trim();
            if (modelName.isEmpty()) continue;

            String href = anyLink.attr("href");
            String productUrl = BASE_URL + href;

            // Series page URLs are /us/<slug> (no "bikes-" prefix), e.g. /us/defy-advanced-0
            String geometryKey = href.replaceFirst("^/[^/]+/", "");

            String priceText = null;
            Element priceEl = td.selectFirst("span.originalprice");
            if (priceEl != null) priceText = priceEl.text();

            BikeSummary bike = new BikeSummary();
            bike.setBrand("Giant");
            bike.setModel(modelName);
            bike.setModelYear(modelYear);
            bike.setProductUrl(productUrl);
            bike.setPriceText(priceText);
            bike.setGeometryKey(geometryKey);
            bike.setDiscipline(discipline);

            results.add(bike);
        }

        return results;
    }

    /**
     * Parses the geometry table from a Giant product page.
     * Returns one BikeGeometry per size column.
     */
    public static List<BikeGeometry> parseGeometryTableDoc(Document doc, String geometryKey) {
        Element table = doc.selectFirst("table.geometry");
        if (table == null) {
            log.warn("Geometry table not found for key='{}' — page structure may have changed", geometryKey);
            return List.of();
        }

        // Size labels from the header row
        List<String> sizes = new ArrayList<>();
        for (Element th : table.select("thead tr.heading th[name=framesize]")) {
            sizes.add(th.text().trim());
        }
        if (sizes.isEmpty()) return List.of();

        // One BikeGeometry per size
        List<BikeGeometry> geometries = new ArrayList<>();
        for (String size : sizes) {
            BikeGeometry geo = new BikeGeometry();
            geo.setBikeGeometryKey(geometryKey);
            geo.setSizeLabel(size);
            geometries.add(geo);
        }

        // Each tr.property is one measurement row
        for (Element row : table.select("tbody tr.property")) {
            Element nameCell = row.selectFirst("td.name");
            if (nameCell == null) continue;
            String label = nameCell.text().toLowerCase();

            List<Element> valueCells = row.select("td.value");

            // Tyre/wheel size is a string (e.g. "700 x 25c"), handle before numeric parsing
            if (label.contains("tyre") || label.contains("tire") || label.contains("wheel size")) {
                if (!valueCells.isEmpty()) {
                    String tyreText = valueCells.get(0).text().trim();
                    if (!tyreText.isEmpty()) {
                        geometries.forEach(g -> g.setWheelSize(tyreText));
                    }
                }
                continue;
            }

            // loop through each size and extract the geometry values for that size
            for (int i = 0; i < Math.min(sizes.size(), valueCells.size()); i++) {
                Element cell = valueCells.get(i);
                // Prefer mm span; angle rows use span.degrees with a ° symbol instead
                Element valueSpan = cell.selectFirst("span.value-mm");
                if (valueSpan == null) valueSpan = cell.selectFirst("span.degrees");
                if (valueSpan == null) valueSpan = cell.selectFirst("span.value:not(.value-inch)");
                if (valueSpan == null) continue;

                Double value = parseDouble(valueSpan.text());
                applyGeometryValue(geometries.get(i), label, value);
            }
        }

        return geometries;
    }

    private static void applyGeometryValue(BikeGeometry geo, String label, Double value) {
        // Check longer/more specific strings before shorter ones to avoid partial matches
        if (label.contains("seat tube length"))         geo.setSeatTubeLength(value);
        else if (label.contains("seat tube angle"))     geo.setSeatTubeAngleEffective(value);
        else if (label.contains("head tube length"))    geo.setHeadTubeLength(value);
        else if (label.contains("head tube angle"))     geo.setHeadTubeAngle(value);
        else if (label.contains("top tube"))            geo.setTopTubeEffective(value);
        else if (label.contains("fork rake")
                || label.contains("fork offset"))       geo.setForkOffset(value);
        else if (label.contains("trail"))               geo.setTrail(value);
        else if (label.contains("wheelbase"))           geo.setWheelbase(value);
        else if (label.contains("chain stay"))          geo.setChainstay(value);
        else if (label.contains("bottom bracket"))      geo.setBbDrop(value);
        else if (label.contains("stack"))               geo.setStack(value);
        else if (label.contains("reach"))               geo.setReach(value);
        else if (label.contains("stand over"))          geo.setStandover(value);
        // Handlebar width, stem length, crank length — not in BikeGeometry, intentionally ignored
    }

    private static Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim().replaceAll("[^\\d.,-]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer extractYear(String classAttr) {
        Matcher m = YEAR_PATTERN.matcher(classAttr);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private static Discipline inferDiscipline(String classAttr) {
        if (classAttr.contains("surface-road-bikes")) return Discipline.ROAD;
        if (classAttr.contains("surface-gravel-bikes")) return Discipline.GRAVEL;
        if (classAttr.contains("surface-mountain-bikes")) {
            if (classAttr.contains("best-for-cross-country")) return Discipline.XC;
            if (classAttr.contains("best-for-trail")) return Discipline.TRAIL;
            if (classAttr.contains("best-for-enduro")) return Discipline.ENDURO;
            if (classAttr.contains("best-for-downhill")) return Discipline.DH;
        }
        return Discipline.UNKNOWN;
    }
}
