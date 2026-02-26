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
                    geometry_key, size_label,
                    stability_index, agility_index, comfort_index, aggression_index, handling_index,
                    stability_z_index, agility_z_index, comfort_z_index, aggression_z_index, handling_z_index)
                VALUES (
                    :geometryKey, :sizeLabel,
                    :stabilityIndex, :agilityIndex, :comfortIndex, :aggressionIndex, :handlingIndex,
                    :stabilityZIndex, :agilityZIndex, :comfortZIndex, :aggressionZIndex, :handlingZIndex)
                ON CONFLICT (geometry_key, size_label) DO UPDATE SET
                    stability_index    = EXCLUDED.stability_index,
                    agility_index      = EXCLUDED.agility_index,
                    comfort_index      = EXCLUDED.comfort_index,
                    aggression_index   = EXCLUDED.aggression_index,
                    handling_index     = EXCLUDED.handling_index,
                    stability_z_index  = EXCLUDED.stability_z_index,
                    agility_z_index    = EXCLUDED.agility_z_index,
                    comfort_z_index    = EXCLUDED.comfort_z_index,
                    aggression_z_index = EXCLUDED.aggression_z_index,
                    handling_z_index   = EXCLUDED.handling_z_index
                """;

        SqlParameterSource[] params = rideCharacters.stream()
                .map(rc -> new MapSqlParameterSource()
                        .addValue("geometryKey", rc.getGeometryKey())
                        .addValue("sizeLabel", rc.getSizeLabel())
                        .addValue("stabilityIndex", rc.getStabilityIndex())
                        .addValue("agilityIndex", rc.getAgilityIndex())
                        .addValue("comfortIndex", rc.getComfortIndex())
                        .addValue("aggressionIndex", rc.getAggressionIndex())
                        .addValue("handlingIndex", rc.getHandlingIndex())
                        .addValue("stabilityZIndex", rc.getStabilityZIndex())
                        .addValue("agilityZIndex", rc.getAgilityZIndex())
                        .addValue("comfortZIndex", rc.getComfortZIndex())
                        .addValue("aggressionZIndex", rc.getAggressionZIndex())
                        .addValue("handlingZIndex", rc.getHandlingZIndex()))
                .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(sql, params);
    }
}
