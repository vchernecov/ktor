CREATE TABLE entity
(
    id    BIGSERIAL PRIMARY KEY,
    title TEXT UNIQUE NOT NULL
);

CREATE INDEX entity_title_idx ON entity (title);