DROP TABLE IF EXISTS users, categories, events, compilations, events_compilations, requests, comments;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    "name"  VARCHAR(100)                            NOT NULL,
    email VARCHAR(100)                            NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    "name" VARCHAR(100)                            NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation         VARCHAR(2000)               NOT NULL,
    category_id        BIGINT                      NOT NULL REFERENCES categories ON DELETE CASCADE,
    confirmed_requests BIGINT,
    createdOn          TIMESTAMP WITHOUT TIME ZONE,
    description        VARCHAR(7000),
    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    initiator_id       BIGINT                      NOT NULL REFERENCES users ON DELETE CASCADE,
    lat                REAL                        NOT NULL,
    lon                REAL                        NOT NULL,
    paid               BOOL                        NOT NULL,
    participant_limit  BIGINT,
    publishedOn       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOL,
    state              VARCHAR(64),
    title              VARCHAR(120)                NOT NULL,
    views              BIGINT
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    title  VARCHAR(120)                            NOT NULL UNIQUE,
    pinned BOOLEAN                                 NOT NULL
);

CREATE TABLE IF NOT EXISTS events_compilations
(
    event_id       BIGINT REFERENCES events (id) ON DELETE CASCADE,
    compilation_id BIGINT REFERENCES compilations (id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, compilation_id)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    event_id     BIGINT                      NOT NULL REFERENCES events (id),
    requester_id BIGINT                      NOT NULL REFERENCES users (id),
    status       VARCHAR(30)                 NOT NULL,
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    UNIQUE (event_id, requester_id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    text VARCHAR(3000) NOT NULL,
    author_id BIGINT  NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    event_id  BIGINT  NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT  now()
    );