DROP TABLE IF EXISTS review;

CREATE TABLE review (
                        review_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                        order_id BIGINT NOT NULL,

                        review_content TEXT NOT NULL,

                        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                        UNIQUE KEY uk_review_order_id (order_id)
);