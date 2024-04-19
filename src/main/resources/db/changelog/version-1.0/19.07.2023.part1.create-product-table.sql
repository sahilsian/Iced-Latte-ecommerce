CREATE TABLE IF NOT EXISTS product
(
    id             UUID,
    name           VARCHAR(64) NOT NULL,
    description    TEXT,
    price          DECIMAL     NOT NULL CHECK (price > 0),
    quantity       INT         NOT NULL CHECK (quantity >= 0),
    active         BOOLEAN     NOT NULL,
    average_rating DECIMAL     CHECK (average_rating >= 0 AND average_rating < 6) DEFAULT 0,
    reviews_count  INT         CHECK (reviews_count >= 0) DEFAULT 0,
    PRIMARY KEY (id)
);