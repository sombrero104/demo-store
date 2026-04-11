CREATE TABLE account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(200) NOT NULL,
    nickname VARCHAR(100),
    enabled BOOLEAN NOT NULL,
    created_date DATETIME(6) NOT NULL,
    modified_date DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_account_email UNIQUE (email)
);

CREATE TABLE role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_code UNIQUE (code)
);

CREATE TABLE role_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_group_code UNIQUE (code)
);

CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE product_color (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    color VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_color_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE product_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_color_id BIGINT NOT NULL,
    size VARCHAR(20) NOT NULL,
    price INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_option_color FOREIGN KEY (product_color_id) REFERENCES product_color (id)
);

CREATE TABLE product_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_color_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_main BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_image_color FOREIGN KEY (product_color_id) REFERENCES product_color (id)
);

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    order_date DATETIME(6),
    status VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE order_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orders_id BIGINT NOT NULL,
    product_option_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price INTEGER NOT NULL,
    status VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_orders FOREIGN KEY (orders_id) REFERENCES orders (id),
    CONSTRAINT fk_order_item_option FOREIGN KEY (product_option_id) REFERENCES product_option (id)
);

CREATE TABLE role_group_role (
    role_group_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT uk_role_group_role UNIQUE (role_group_id, role_id),
    CONSTRAINT fk_role_group_role_group FOREIGN KEY (role_group_id) REFERENCES role_group (id),
    CONSTRAINT fk_role_group_role_role FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE TABLE account_role_group (
    account_id BIGINT NOT NULL,
    role_group_id BIGINT NOT NULL,
    CONSTRAINT uk_account_role_group UNIQUE (account_id, role_group_id),
    CONSTRAINT fk_account_role_group_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_account_role_group_group FOREIGN KEY (role_group_id) REFERENCES role_group (id)
);

CREATE TABLE jwt_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    refresh_token VARCHAR(1000) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_jwt_token_account_id UNIQUE (account_id),
    CONSTRAINT fk_jwt_token_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE INDEX idx_product_color_product_id ON product_color (product_id);
CREATE INDEX idx_product_option_color_id ON product_option (product_color_id);
CREATE INDEX idx_product_image_color_id ON product_image (product_color_id);
CREATE INDEX idx_orders_account_id ON orders (account_id);
CREATE INDEX idx_order_item_orders_id ON order_item (orders_id);
CREATE INDEX idx_order_item_product_option_id ON order_item (product_option_id);
