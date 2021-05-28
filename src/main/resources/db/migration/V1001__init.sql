CREATE TABLE band
(
    id    BIGSERIAL PRIMARY KEY,
    name  CHARACTER VARYING(100) NOT NULL,
    maKey BIGINT
);


CREATE TABLE album
(
    id          BIGSERIAL PRIMARY KEY,
    band_id     BIGINT REFERENCES band (id) NOT NULL,

    position    INTEGER                     NOT NULL DEFAULT 999,
    type        CHARACTER VARYING(50),
    typeCount   INTEGER                              DEFAULT 99,
    name        CHARACTER VARYING(200),
    year        INTEGER                     NOT NULL DEFAULT 9999,

    status      INTEGER                     NOT NULL DEFAULT 0,

    maType      CHARACTER VARYING(50),
    maTypeCount INTEGER,
    maName      CHARACTER VARYING(200)
);
