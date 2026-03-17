package com.bikeadvisor.bike_advisor.repository;

import com.bikeadvisor.bike_advisor.model.RideCharacter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RideCharacterRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RideCharacterRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveAll(List<RideCharacter> rideCharacters) {
        String sql = """
                INSERT INTO ride_character (
                    geometry_key, size_label, size_bucket,
                    stability_index, aero_index, agility_index)
                VALUES (
                    :geometryKey, :sizeLabel, :sizeBucket,
                    :stabilityIndex, :aeroIndex, :agilityIndex)
                ON CONFLICT (geometry_key, size_label) DO UPDATE SET
                    size_bucket     = EXCLUDED.size_bucket,
                    stability_index = EXCLUDED.stability_index,
                    aero_index      = EXCLUDED.aero_index,
                    agility_index   = EXCLUDED.agility_index
                """;

        SqlParameterSource[] params = rideCharacters.stream()
                .map(rc -> new MapSqlParameterSource()
                        .addValue("geometryKey", rc.getGeometryKey())
                        .addValue("sizeLabel", rc.getSizeLabel())
                        .addValue("sizeBucket", rc.getSizeBucket())
                        .addValue("stabilityIndex", rc.getStabilityIndex())
                        .addValue("aeroIndex", rc.getAeroIndex())
                        .addValue("agilityIndex", rc.getAgilityIndex()))
                .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(sql, params);
    }
}
