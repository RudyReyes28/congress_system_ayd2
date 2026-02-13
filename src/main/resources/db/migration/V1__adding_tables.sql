-- ============================================
-- AUTHENTICATION & USERS
-- ============================================

CREATE TABLE person (
    id_person INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    organization VARCHAR(255) NOT NULL,
    photo_url VARCHAR(500)
);

CREATE TABLE user (
    id_user BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    identification_number VARCHAR(50) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    wallet_balance DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE role (
    id_role INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE user_role (
    id_user_role BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_user BIGINT NOT NULL,
    id_role INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role_user FOREIGN KEY (id_user) REFERENCES user(id_user),
    CONSTRAINT fk_user_role_role FOREIGN KEY (id_role) REFERENCES role(id_role)
);

CREATE TABLE refresh_token (
    id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_user BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (id_user) REFERENCES user(id_user)
);

-- ============================================
-- INSTITUTIONS
-- ============================================

CREATE TABLE institution (
    id_institution BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    address VARCHAR(500),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE system_configuration (
    id_config INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- CONGRESS
-- ============================================

CREATE TABLE congress (
    id_congress BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_institution BIGINT NOT NULL,
    congress_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location VARCHAR(500) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_congress_institution FOREIGN KEY (id_institution) REFERENCES institution(id_institution)
);

CREATE TABLE congress_administrator (
    id_congress_admin BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress),
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);

CREATE TABLE scientific_committee (
    id_committee_member BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress),
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);

-- ============================================
-- ROOMS & ACTIVITIES
-- ============================================

CREATE TABLE room (
    id_room BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    room_name VARCHAR(100) NOT NULL,
    room_code VARCHAR(50),
    capacity INT,
    location VARCHAR(255),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress)
);

CREATE TABLE activity_type (
    id_activity_type INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE activity (
    id_activity BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    id_room BIGINT NOT NULL,
    id_activity_type INT NOT NULL,
    activity_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_capacity INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress),
    FOREIGN KEY (id_room) REFERENCES room(id_room),
    FOREIGN KEY (id_activity_type) REFERENCES activity_type(id_activity_type)
);

CREATE TABLE activity_presenter (
    id_activity_presenter BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_activity BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    is_invited_speaker BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_activity) REFERENCES activity(id_activity) ON DELETE CASCADE,
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);

-- ============================================
-- SUBMISSIONS
-- ============================================

CREATE TABLE call_for_papers (
    id_call BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    call_name VARCHAR(255) NOT NULL,
    description TEXT,
    open_date DATETIME NOT NULL,
    close_date DATETIME NOT NULL,
    is_open BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress)
);

CREATE TABLE submission_status (
    id_status INT AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE submission (
    id_submission BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_call BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    id_activity_type INT NOT NULL,
    id_status INT DEFAULT 1,
    submission_title VARCHAR(255) NOT NULL,
    abstract TEXT NOT NULL,
    file_url VARCHAR(500),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_call) REFERENCES call_for_papers(id_call),
    FOREIGN KEY (id_user) REFERENCES user(id_user),
    FOREIGN KEY (id_activity_type) REFERENCES activity_type(id_activity_type),
    FOREIGN KEY (id_status) REFERENCES submission_status(id_status)
);

CREATE TABLE submission_evaluation (
    id_evaluation BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_submission BIGINT NOT NULL,
    id_evaluator BIGINT NOT NULL,
    comments TEXT,
    is_approved BOOLEAN NOT NULL,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_submission) REFERENCES submission(id_submission),
    FOREIGN KEY (id_evaluator) REFERENCES user(id_user)
);

-- ============================================
-- REGISTRATIONS & PAYMENTS
-- ============================================

CREATE TABLE registration (
    id_registration BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    registration_date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress),
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);

CREATE TABLE wallet_transaction (
    id_transaction BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_user BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    related_registration_id BIGINT,
    transaction_date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_user) REFERENCES user(id_user),
    FOREIGN KEY (related_registration_id) REFERENCES registration(id_registration)
);

-- ============================================
-- WORKSHOP RESERVATIONS
-- ============================================

CREATE TABLE workshop_reservation (
    id_reservation BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_activity BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_activity) REFERENCES activity(id_activity),
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);

-- ============================================
-- ATTENDANCE
-- ============================================

CREATE TABLE participation_type (
    id_participation_type INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE attendance (
    id_attendance BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_activity BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    id_participation_type INT NOT NULL,
    recorded_by BIGINT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_activity) REFERENCES activity(id_activity),
    FOREIGN KEY (id_user) REFERENCES user(id_user),
    FOREIGN KEY (id_participation_type) REFERENCES participation_type(id_participation_type),
    FOREIGN KEY (recorded_by) REFERENCES user(id_user)
);

-- ============================================
-- CERTIFICATES
-- ============================================

CREATE TABLE certificate (
    id_certificate BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_congress BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    certificate_type VARCHAR(50) NOT NULL,
    id_activity BIGINT,
    certificate_url VARCHAR(500),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_congress) REFERENCES congress(id_congress),
    FOREIGN KEY (id_user) REFERENCES user(id_user),
    FOREIGN KEY (id_activity) REFERENCES activity(id_activity)
);