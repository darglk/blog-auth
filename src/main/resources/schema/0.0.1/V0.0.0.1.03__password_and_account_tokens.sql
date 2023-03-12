CREATE TABLE password_reset_token (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT current_timestamp(3)
);

CREATE TABLE account_activation_token (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT current_timestamp(3)
);