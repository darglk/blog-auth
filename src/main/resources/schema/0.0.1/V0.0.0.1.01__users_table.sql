CREATE TABLE users (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(100) NOT NULL UNIQUE,
  enabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE authorities (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(36) NOT NULL UNIQUE
);

CREATE TABLE users_authorities(
    user_id VARCHAR(36) NOT NULL REFERENCES users (id),
    authority_id VARCHAR(36) NOT NULL REFERENCES authorities (id)
);

CREATE UNIQUE INDEX users_authorities_idx ON users_authorities (user_id, authority_id);