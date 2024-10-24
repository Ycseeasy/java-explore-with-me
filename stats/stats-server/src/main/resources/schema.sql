DROP TABLE IF EXISTS endpointhit CASCADE;

CREATE TABLE IF NOT EXISTS endpointhit (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app VARCHAR(50) NOT NULL,
    uri VARCHAR(1000) NOT NULL,
    ip VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WI THOUT TIME ZONE
);