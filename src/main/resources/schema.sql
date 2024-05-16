DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;

CREATE TABLE IF NOT EXISTS users (
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name    VARCHAR        NOT NULL,
    email   VARCHAR UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name            VARCHAR NOT NULL,
    is_available    BOOLEAN NOT NULL,
    owner_id        BIGINT NOT NULL,
    CONSTRAINT fk_items_to_users FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    start_time  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status      varchar NOT NULL,
    item_id     BIGINT NOT NULL,
    booker_id   BIGINT NOT NULL,
    CONSTRAINT fk_bookings_to_items FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_to_users FOREIGN KEY(booker_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    text    VARCHAR NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);