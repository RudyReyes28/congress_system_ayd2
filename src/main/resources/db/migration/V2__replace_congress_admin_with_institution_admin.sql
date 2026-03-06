DROP TABLE IF EXISTS congress_administrator;
CREATE TABLE institution_administrator (
    id_institution_admin BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_institution BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inst_admin_institution FOREIGN KEY (id_institution) REFERENCES institution(id_institution),
    CONSTRAINT fk_inst_admin_user FOREIGN KEY (id_user) REFERENCES user(id_user)
);