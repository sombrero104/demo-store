SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE order_item;
TRUNCATE TABLE orders;
TRUNCATE TABLE account_role_group;
TRUNCATE TABLE account;
TRUNCATE TABLE role_group_role;
TRUNCATE TABLE role_group;
TRUNCATE TABLE role;
TRUNCATE TABLE product_image;
TRUNCATE TABLE product_option;
TRUNCATE TABLE product_color;
TRUNCATE TABLE product;

SET REFERENTIAL_INTEGRITY TRUE;

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
