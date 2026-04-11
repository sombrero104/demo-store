
-- FK 때문에 TRUNCATE가 막히는 걸 피하려고 잠깐 비활성화
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE order_item;
TRUNCATE TABLE orders;

TRUNCATE TABLE product_image;
TRUNCATE TABLE product_option;
TRUNCATE TABLE product_color;
TRUNCATE TABLE product;

TRUNCATE TABLE jwt_token;

-- N:M 매핑 테이블
TRUNCATE TABLE account_role_group;
TRUNCATE TABLE role_group_role;

TRUNCATE TABLE account;

-- 권한 테이블
TRUNCATE TABLE role_group;
TRUNCATE TABLE role;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- ROLE (권한 단위) 시드
-- id를 명시하면 뒤에 매핑 INSERT가 편해짐
-- (id 컬럼이 auto_increment라는 가정)
-- ----------------------------
INSERT INTO role (id, code, name, enabled) VALUES
(1, 'ORDER_READ',   '주문 조회', 1),
(2, 'ORDER_WRITE',  '주문 생성/취소', 1),
(3, 'PRODUCT_READ', '상품 조회', 1),
(4, 'PRODUCT_WRITE','상품 등록/수정', 1);

-- ----------------------------
-- ROLE_GROUP (권한 묶음) 시드
-- 가입 기본 그룹(USER)은 반드시 존재해야 함
-- ----------------------------
INSERT INTO role_group (id, code, name, enabled) VALUES
(1, 'USER',  '일반 사용자', 1),
(2, 'ADMIN', '관리자', 1);

-- ----------------------------
-- ROLE_GROUP ↔ ROLE 매핑
-- ----------------------------
INSERT INTO role_group_role (role_group_id, role_id) VALUES
-- USER 그룹 권한
(1, 1),
(1, 2),
(1, 3),

-- ADMIN 그룹 권한
(2, 1),
(2, 2),
(2, 3),
(2, 4);

-- ----------------------------
-- 테스트 계정(비밀번호는 BCrypt 해시로 넣어야 로그인 됨)
-- 아래 '<<BCrypt 해시>>' 부분 교체 필요
-- ----------------------------
INSERT INTO account (id, email, password, nickname, enabled, created_date, modified_date) VALUES
(1, 'user@store.com',  '$2a$10$yPKjbtqNwi86s6DDU/OpMuQmxaBAUGxpOCvi8RtxiKRmZZs2c1BKC',  'user',  1, NOW(), NOW()),
(2, 'admin@store.com', '$2a$10$yPKjbtqNwi86s6DDU/OpMuQmxaBAUGxpOCvi8RtxiKRmZZs2c1BKC',  'admin', 1, NOW(), NOW());

-- account ↔ role_group 매핑
INSERT INTO account_role_group (account_id, role_group_id) VALUES
(1, 1),
(2, 2);

-- ----------------------------
-- 상품 시드 (기존 로직 유지)
-- ----------------------------
INSERT INTO product (id, name) VALUES (1, '셔츠');
INSERT INTO product (id, name) VALUES (2, '바지');

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
