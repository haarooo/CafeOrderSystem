DROP TABLE IF EXISTS review;

CREATE TABLE review (
                        review_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                        order_id BIGINT NOT NULL,

                        review_content TEXT NOT NULL,
                        analysis_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        analysis_result_json LONGTEXT NULL,
                        analyzed_at DATETIME(6) NULL,
                        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                        UNIQUE KEY uk_review_order_id (order_id)
);

DROP TABLE IF EXISTS outbox;

CREATE TABLE IF NOT EXISTS outbox (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      event_id VARCHAR(100) NOT NULL UNIQUE,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    kafka_key VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6) NULL,
    INDEX idx_outbox_status_id (status, id),
    INDEX idx_outbox_event_type (event_type),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
    );


DROP TABLE IF EXISTS processed_event;

CREATE TABLE IF NOT EXISTS processed_event (
                                               event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    INDEX idx_processed_event_type (event_type)
    );


DROP TABLE IF EXISTS menu_read;
CREATE TABLE IF NOT EXISTS menu_read (
                                         menu_id BIGINT PRIMARY KEY,
                                         menu_name VARCHAR(100) NOT NULL,
    menu_price INT NOT NULL,
    menu_image VARCHAR(500) NULL,
    menu_category VARCHAR(50) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL
    );

DROP TABLE IF EXISTS reply_read;
CREATE TABLE IF NOT EXISTS reply_read (
                                          customer_review_id BIGINT PRIMARY KEY,
                                          order_id BIGINT NULL,
                                          reply_content TEXT NULL,
                                          has_reply BOOLEAN NOT NULL DEFAULT FALSE,
                                          replied_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL
    );

DROP TABLE IF EXISTS reviewable_order_read;
CREATE TABLE IF NOT EXISTS reviewable_order_read (
                                                     order_id BIGINT PRIMARY KEY,
                                                     order_price INT NULL,
                                                     created_at DATETIME(6) NULL,
    review_written BOOLEAN NOT NULL DEFAULT FALSE
    );