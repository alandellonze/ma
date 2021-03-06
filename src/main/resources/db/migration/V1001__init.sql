CREATE TABLE band
(
  id BIGSERIAL NOT NULL,
  name CHARACTER VARYING(100),
  maKey BIGINT,
  CONSTRAINT band_pkey PRIMARY KEY (id)
);

CREATE TABLE album
(
  id BIGSERIAL NOT NULL,
  band_id BIGINT REFERENCES band(id),
  position INTEGER DEFAULT 999,
  type CHARACTER VARYING(50),
  typeCount INTEGER DEFAULT 99,
  name CHARACTER VARYING(200),
  year INTEGER DEFAULT 9999,
  CONSTRAINT album_pkey PRIMARY KEY (id)
);
