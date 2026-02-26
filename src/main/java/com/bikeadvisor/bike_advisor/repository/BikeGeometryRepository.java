package com.bikeadvisor.bike_advisor.repository;

import com.bikeadvisor.bike_advisor.dto.ScatterPoint;
import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BikeGeometryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public BikeGeometryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveAll(List<BikeGeometry> geometries) {
        String sql = """
                INSERT INTO bike_geometry (
                    bike_geometry_key, size_label, wheel_size, reach, stack,
                    top_tube_effective, head_tube_angle, seat_tube_angle_effective, head_tube_length,
                    seat_tube_length, chainstay, wheelbase, bb_drop, fork_offset, trail, standover)
                VALUES (
                    :bikeGeometryKey, :sizeLabel, :wheelSize, :reach, :stack,
                    :topTubeEffective, :headTubeAngle, :seatTubeAngleEffective, :headTubeLength,
                    :seatTubeLength, :chainstay, :wheelbase, :bbDrop, :forkOffset, :trail, :standover)
                ON CONFLICT (bike_geometry_key, size_label) DO UPDATE SET
                    wheel_size                = EXCLUDED.wheel_size,
                    reach                     = EXCLUDED.reach,
                    stack                     = EXCLUDED.stack,
                    top_tube_effective        = EXCLUDED.top_tube_effective,
                    head_tube_angle           = EXCLUDED.head_tube_angle,
                    seat_tube_angle_effective = EXCLUDED.seat_tube_angle_effective,
                    head_tube_length          = EXCLUDED.head_tube_length,
                    seat_tube_length          = EXCLUDED.seat_tube_length,
                    chainstay                 = EXCLUDED.chainstay,
                    wheelbase                 = EXCLUDED.wheelbase,
                    bb_drop                   = EXCLUDED.bb_drop,
                    fork_offset               = EXCLUDED.fork_offset,
                    trail                     = EXCLUDED.trail,
                    standover                 = EXCLUDED.standover
                """;

        SqlParameterSource[] params = geometries.stream()
                .map(g -> new MapSqlParameterSource()
                        .addValue("bikeGeometryKey", g.getBikeGeometryKey())
                        .addValue("sizeLabel", g.getSizeLabel())
                        .addValue("wheelSize", g.getWheelSize())
                        .addValue("reach", g.getReach())
                        .addValue("stack", g.getStack())
                        .addValue("topTubeEffective", g.getTopTubeEffective())
                        .addValue("headTubeAngle", g.getHeadTubeAngle())
                        .addValue("seatTubeAngleEffective", g.getSeatTubeAngleEffective())
                        .addValue("headTubeLength", g.getHeadTubeLength())
                        .addValue("seatTubeLength", g.getSeatTubeLength())
                        .addValue("chainstay", g.getChainstay())
                        .addValue("wheelbase", g.getWheelbase())
                        .addValue("bbDrop", g.getBbDrop())
                        .addValue("forkOffset", g.getForkOffset())
                        .addValue("trail", g.getTrail())
                        .addValue("standover", g.getStandover()))
                .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(sql, params);
    }

    public List<ScatterPoint> findScatterData(String brand, String discipline, String wheelSize) {
        String sql = """
                SELECT bg.bike_geometry_key,
                       bg.size_label,
                       bg.stack,
                       bg.reach,
                       bg.wheel_size,
                       COUNT(DISTINCT b.product_url) AS bike_count
                FROM bike_geometry bg
                LEFT JOIN bike b ON b.geometry_key = bg.bike_geometry_key
                WHERE bg.stack IS NOT NULL
                  AND bg.reach IS NOT NULL
                  AND (CAST(:brand AS TEXT) IS NULL OR b.brand = :brand)
                  AND (CAST(:discipline AS TEXT) IS NULL OR b.discipline = :discipline)
                  AND (CAST(:wheelSize AS TEXT) IS NULL OR LOWER(bg.wheel_size) = LOWER(:wheelSize))
                GROUP BY bg.bike_geometry_key, bg.size_label, bg.stack, bg.reach, bg.wheel_size
                ORDER BY bg.bike_geometry_key, bg.size_label
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("brand", brand)
                .addValue("discipline", discipline)
                .addValue("wheelSize", wheelSize);

        return jdbc.query(sql, params, (rs, rowNum) -> new ScatterPoint(
                rs.getString("bike_geometry_key"),
                rs.getString("size_label"),
                rs.getDouble("stack"),
                rs.getDouble("reach"),
                rs.getString("wheel_size"),
                rs.getInt("bike_count")
        ));
    }

    public List<String> findDistinctWheelSizes() {
        String sql = "SELECT DISTINCT wheel_size FROM bike_geometry WHERE wheel_size IS NOT NULL ORDER BY wheel_size";
        return jdbc.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> rs.getString("wheel_size"));
    }
}
