-- 1. subscription_category
CREATE TABLE subscription_category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. subscription_item
CREATE TABLE subscription_item (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL ,
    item_name VARCHAR(255) NOT NULL ,
    FOREIGN KEY (category_id) REFERENCES subscription_category(category_id),
    UNIQUE (category_id, item_name)
);

-- 3. user
CREATE TABLE `user` (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL UNIQUE,
    user_state ENUM('ACTIVE', 'INACTIVE') NOT NULL,
    profile_image VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. card
CREATE TABLE card (
    card_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_company VARCHAR(50) NOT NULL UNIQUE
);

-- 5. payment_method
CREATE TABLE payment_method (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    card_name VARCHAR(255) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1 CHECK (is_active IN (0,1)),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    custom_card_company VARCHAR(50) NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id),
    FOREIGN KEY (card_id) REFERENCES `card`(card_id),
    CHECK (
        (card_id IS NOT NULL AND custom_card_company IS NULL)
        OR
        (card_id IS NULL AND custom_card_company IS NOT NULL)
    )
);

-- 6. add_subscription
CREATE TABLE add_subscription (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    payment_id BIGINT NOT NULL,
    price INT NOT NULL,
    billing_cycle ENUM('1M', '1Y') NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    use_yn ENUM('Y','N') NOT NULL DEFAULT 'Y',
    FOREIGN KEY (user_id) REFERENCES `user`(user_id),
    FOREIGN KEY (item_id) REFERENCES subscription_item(item_id),
    FOREIGN KEY (payment_id) REFERENCES payment_method(payment_id)
);

-- 7. community_posts
CREATE TABLE community_posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    scrap_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id)
);

-- 8. community_scrap
CREATE TABLE community_scrap (
    scrap_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL , 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	 FOREIGN KEY (post_id) REFERENCES community_posts(post_id),
	 FOREIGN KEY (user_id) REFERENCES `user`(user_id),
	 UNIQUE (user_id, post_id)
);

-- 9. notifications
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0 CHECK (is_read IN (0,1)),
    is_closed TINYINT(1) NOT NULL DEFAULT 0 CHECK (is_closed IN (0,1)),
    notify_type ENUM('D3', 'D0') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    content TEXT NOT NULL,
    target_date DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id),
    FOREIGN KEY (subscription_id) REFERENCES add_subscription(subscription_id),
    UNIQUE (user_id, subscription_id, notify_type, target_date)
);

-- 10. recommend_report
CREATE TABLE recommend_report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_title VARCHAR(200) NOT NULL,
    request_note TEXT NOT NULL,
    generated_content LONGTEXT DEFAULT NULL,
    total_monthly_price INT NOT NULL DEFAULT 0,
    max_monthly_budget INT NOT NULL,
    mandatory_items_json TEXT NOT NULL,
    optional_items_json TEXT DEFAULT NULL,
    report_status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id)
);

-- 11. recommend_subscription_item
CREATE TABLE recommend_subscription_item (
    recommend_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    category VARCHAR(100) DEFAULT NULL,
    monthly_price INT NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES recommend_report(report_id)
);


