package com.bikeadvisor.bike_advisor.repository;

import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Discipline;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BikeSummaryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public BikeSummaryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveAll(List<BikeSummary> bikes) {
        String sql = """
                INSERT INTO bike (product_url, id, brand, model, discipline, model_year, price, geometry_key)
                VALUES (:productUrl, :id, :brand, :model, :discipline, :modelYear, :price, :geometryKey)
                ON CONFLICT (product_url) DO UPDATE SET
                    id           = EXCLUDED.id,
                    brand        = EXCLUDED.brand,
                    model        = EXCLUDED.model,
                    discipline   = EXCLUDED.discipline,
                    model_year   = EXCLUDED.model_year,
                    price        = EXCLUDED.price,
                    geometry_key = EXCLUDED.geometry_key
                """;

        SqlParameterSource[] params = bikes.stream()
                .map(b -> new MapSqlParameterSource()
                        .addValue("productUrl", b.getProductUrl())
                        .addValue("id", b.getId())
                        .addValue("brand", b.getBrand())
                        .addValue("model", b.getModel())
                        .addValue("discipline", b.getDiscipline() != null ? b.getDiscipline().name() : null)
                        .addValue("modelYear", b.getModelYear())
                        .addValue("price", b.getPrice())
                        .addValue("geometryKey", b.getGeometryKey()))
                .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(sql, params);
    }

    public List<BikeSummary> findByFilters(String brand, String discipline, String search,
                                           Double minPrice, Double maxPrice) {
        String updatedBrand = capitalizeFirstLetter(brand);
        String sql = """
                SELECT b.brand, b.model, b.discipline, b.model_year, b.price, b.product_url, b.geometry_key,
                       AVG(rc.stability_index) AS stability_index,
                       AVG(rc.aero_index)      AS aero_index,
                       AVG(rc.agility_index)   AS agility_index
                FROM bike b
                LEFT JOIN ride_character rc ON rc.geometry_key = b.geometry_key
                WHERE (CAST(:brand AS TEXT) IS NULL OR b.brand = :brand)
                  AND (CAST(:discipline AS TEXT) IS NULL OR b.discipline = :discipline)
                  AND (CAST(:search AS TEXT) IS NULL OR LOWER(b.model) LIKE '%' || LOWER(:search) || '%'
                                                     OR LOWER(b.brand) LIKE '%' || LOWER(:search) || '%')
                  AND (CAST(:minPrice AS DOUBLE PRECISION) IS NULL OR b.price >= :minPrice)
                  AND (CAST(:maxPrice AS DOUBLE PRECISION) IS NULL OR b.price <= :maxPrice)
                GROUP BY b.product_url, b.brand, b.model, b.discipline, b.model_year, b.price, b.geometry_key
                ORDER BY b.brand, b.model
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("brand", updatedBrand)
                .addValue("discipline", discipline)
                .addValue("search", search)
                .addValue("minPrice", minPrice)
                .addValue("maxPrice", maxPrice);


        return jdbc.query(sql, params, (rs, rowNum) -> {
            BikeSummary bike = new BikeSummary();
            bike.setBrand(rs.getString("brand"));
            bike.setModel(rs.getString("model"));
            bike.setProductUrl(rs.getString("product_url"));
            bike.setGeometryKey(rs.getString("geometry_key"));
            double price = rs.getDouble("price");
            if (!rs.wasNull()) bike.setPrice(price);
            String disc = rs.getString("discipline");
            if (disc != null) {
                try { bike.setDiscipline(Discipline.valueOf(disc)); } catch (IllegalArgumentException ignored) {}
            }
            int modelYear = rs.getInt("model_year");
            if (!rs.wasNull()) bike.setModelYear(modelYear);
            bike.setStabilityIndex(rs.getObject("stability_index", Double.class));
            bike.setAeroIndex(rs.getObject("aero_index", Double.class));
            bike.setAgilityIndex(rs.getObject("agility_index", Double.class));
            return bike;
        });
    }

    public List<String> findDistinctBrands() {
        String sql = "SELECT DISTINCT brand FROM bike WHERE brand IS NOT NULL ORDER BY brand";
        return jdbc.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> rs.getString("brand"));
    }

    public List<String> findDistinctDisciplines() {
        String sql = "SELECT DISTINCT discipline FROM bike WHERE discipline IS NOT NULL ORDER BY discipline";
        return jdbc.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> rs.getString("discipline"));
    }

    private static String capitalizeFirstLetter(String original) {
        if (original == null || original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}
