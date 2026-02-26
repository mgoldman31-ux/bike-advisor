CREATE TABLE IF NOT EXISTS bike (
    product_url  VARCHAR(500)  NOT NULL,
    id           VARCHAR(255)  UNIQUE,
    brand        VARCHAR(100),
    model        VARCHAR(255),
    discipline   VARCHAR(50),
    model_year   INTEGER,
    price        DOUBLE PRECISION,
    geometry_key VARCHAR(255),
    PRIMARY KEY (product_url)
);

CREATE TABLE IF NOT EXISTS bike_geometry (
    bike_geometry_key         VARCHAR(255)     NOT NULL,
    size_label                VARCHAR(50)      NOT NULL,
    wheel_size                VARCHAR(20),
    reach                     DOUBLE PRECISION,
    stack                     DOUBLE PRECISION,
    top_tube_effective        DOUBLE PRECISION,
    head_tube_angle           DOUBLE PRECISION,
    seat_tube_angle_effective DOUBLE PRECISION,
    head_tube_length          DOUBLE PRECISION,
    seat_tube_length          DOUBLE PRECISION,
    chainstay                 DOUBLE PRECISION,
    wheelbase                 DOUBLE PRECISION,
    bb_drop                   DOUBLE PRECISION,
    fork_offset               DOUBLE PRECISION,
    trail                     DOUBLE PRECISION,
    standover                 DOUBLE PRECISION,
    PRIMARY KEY (bike_geometry_key, size_label)
);

CREATE TABLE IF NOT EXISTS ride_character (
    geometry_key       VARCHAR(255)     NOT NULL,
    size_label         VARCHAR(50)      NOT NULL,
    stability_index    DOUBLE PRECISION,
    agility_index      DOUBLE PRECISION,
    comfort_index      DOUBLE PRECISION,
    aggression_index   DOUBLE PRECISION,
    handling_index     DOUBLE PRECISION,
    stability_z_index  DOUBLE PRECISION,
    agility_z_index    DOUBLE PRECISION,
    comfort_z_index    DOUBLE PRECISION,
    aggression_z_index DOUBLE PRECISION,
    handling_z_index   DOUBLE PRECISION,
    PRIMARY KEY (geometry_key, size_label)
);
