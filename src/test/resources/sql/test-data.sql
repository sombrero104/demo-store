INSERT INTO role (id, code, name, enabled) VALUES
(1, 'ORDER_READ', '주문 조회', 1),
(2, 'ORDER_WRITE', '주문 생성/취소', 1),
(3, 'PRODUCT_READ', '상품 조회', 1),
(4, 'PRODUCT_WRITE', '상품 등록/수정', 1);

INSERT INTO role_group (id, code, name, enabled) VALUES
(1, 'USER', '일반 사용자', 1),
(2, 'ADMIN', '관리자', 1);

INSERT INTO role_group_role (role_group_id, role_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2), (2, 3), (2, 4);

INSERT INTO account (id, email, password, nickname, enabled, created_date, modified_date) VALUES
(1, 'user@store.com', '$2a$10$yPKjbtqNwi86s6DDU/OpMuQmxaBAUGxpOCvi8RtxiKRmZZs2c1BKC', 'user', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'admin@store.com', '$2a$10$yPKjbtqNwi86s6DDU/OpMuQmxaBAUGxpOCvi8RtxiKRmZZs2c1BKC', 'admin', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO account_role_group (account_id, role_group_id) VALUES
(1, 1), (2, 2);

INSERT INTO product (id, name) VALUES
(1, '셔츠'),
(2, '바지');

INSERT INTO product_color (id, product_id, color) VALUES
(1, 1, '빨강'),
(2, 2, '파랑');

INSERT INTO product_option (id, product_color_id, size, price, stock, version) VALUES
(1, 1, 'M', 1000, 1, 0),
(2, 1, 'L', 1200, 5, 0),
(3, 2, 'M', 1500, 7, 0);

INSERT INTO product_image (id, product_color_id, image_url, is_main) VALUES
(1, 1, 'shirt-red.jpg', 1),
(2, 2, 'pants-blue.jpg', 1);
