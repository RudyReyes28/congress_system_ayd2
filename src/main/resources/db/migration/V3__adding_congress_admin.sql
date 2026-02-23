CREATE TABLE congress_administrator (
    id_congress_admin BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress),
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);