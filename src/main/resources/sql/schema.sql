DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS outbox;
DROP TABLE IF EXISTS processed_event;
DROP TABLE IF EXISTS menu_read;
DROP TABLE IF EXISTS reviewable_order_read;

CREATE TABLE review (
                        review_id BIGSERIAL PRIMARY KEY,

                        order_id BIGINT NOT NULL,

                        review_content TEXT NOT NULL,

                        analysis_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        analysis_result_json JSONB NULL,
                        analyzed_at TIMESTAMP NULL,

                        reply_content TEXT NULL,
                        reply_status VARCHAR(30) NOT NULL DEFAULT 'NONE',
                        replied_at TIMESTAMP NULL,
                        reply_updated_at TIMESTAMP NULL,

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT uk_review_order_id UNIQUE (order_id)
);

CREATE TABLE outbox (
                        id BIGSERIAL PRIMARY KEY,

                        event_id VARCHAR(100) NOT NULL UNIQUE,
                        aggregate_type VARCHAR(50) NOT NULL,
                        aggregate_id VARCHAR(64) NOT NULL,
                        event_type VARCHAR(100) NOT NULL,
                        topic VARCHAR(100) NOT NULL,
                        kafka_key VARCHAR(100) NOT NULL,
                        payload JSONB NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'NEW',
                        retry_count INT NOT NULL DEFAULT 0,
                        last_error TEXT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        sent_at TIMESTAMP NULL
);

CREATE INDEX idx_outbox_status_id ON outbox(status, id);
CREATE INDEX idx_outbox_event_type ON outbox(event_type);
CREATE INDEX idx_outbox_aggregate ON outbox(aggregate_type, aggregate_id);

CREATE TABLE processed_event (
                                 event_id VARCHAR(100) PRIMARY KEY,
                                 event_type VARCHAR(100) NOT NULL,
                                 processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_processed_event_type ON processed_event(event_type);

CREATE TABLE menu_read (
                           menu_id BIGINT PRIMARY KEY,
                           menu_name VARCHAR(100) NOT NULL,
                           menu_price INT NOT NULL,
                           menu_image VARCHAR(500) NULL,
                           menu_category VARCHAR(50) NULL,
                           deleted BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at TIMESTAMP NULL,
                           updated_at TIMESTAMP NULL
);

CREATE TABLE reviewable_order_read (
                                       order_id BIGINT PRIMARY KEY,
                                       order_price INT NULL,
                                       created_at TIMESTAMP NULL,
                                       review_written BOOLEAN NOT NULL DEFAULT FALSE
);