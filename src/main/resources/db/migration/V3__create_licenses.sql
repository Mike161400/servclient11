CREATE TABLE IF NOT EXISTS licenses (
                                        id UUID PRIMARY KEY,
                                        license_key VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    device_id VARCHAR(255),
    activated_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_licenses_user
    FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_licenses_license_key
    ON licenses (license_key);

CREATE INDEX IF NOT EXISTS idx_licenses_user_id
    ON licenses (user_id);

CREATE INDEX IF NOT EXISTS idx_licenses_expires_at
    ON licenses (expires_at);