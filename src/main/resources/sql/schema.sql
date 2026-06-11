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



INSERT INTO menu_read
(menu_id, menu_name, menu_price, menu_image, menu_category, deleted, created_at, updated_at)
VALUES
    (1, '아메리카노',      3000, '/images/menu/americano.jpg',        'COFFEE', false, NOW(6), NOW(6)),
    (2, '카페라떼',        3800, '/images/menu/cafe_latte.jpg',       'COFFEE', false, NOW(6), NOW(6)),
    (3, '바닐라라떼',      4300, '/images/menu/vanilla_latte.jpg',    'COFFEE', false, NOW(6), NOW(6)),
    (4, '카페모카',        4300, '/images/menu/cafe_mocha.jpg',       'COFFEE', false, NOW(6), NOW(6)),
    (5, '아메모카',        4000, '/images/menu/ame_mocha.jpg',        'COFFEE', false, NOW(6), NOW(6)),
    (6, '카라멜 마키아또', 4000, '/images/menu/caramel_macchiato.jpg','COFFEE', false, NOW(6), NOW(6)),
    (7, '초코라떼',        4000, '/images/menu/choco_latte.jpg',      'LATTE',  false, NOW(6), NOW(6)),
    (8, '딸기 에이드',     4500, '/images/menu/strawberry_ade.jpg',   'ADE',    false, NOW(6), NOW(6)),
    (9, '콜드브루',        4500, '/images/menu/cold_brew.jpg',        'COFFEE', false, NOW(6), NOW(6));

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