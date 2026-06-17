-- 删除旧CRM商品表，使用新的产品管理表(pm_product)替代
-- 注意：执行前请确保数据已迁移至pm_product表

-- 按外键依赖顺序删除
DROP TABLE IF EXISTS product_price_field_blob;
DROP TABLE IF EXISTS product_price_field;
DROP TABLE IF EXISTS product_price;
DROP TABLE IF EXISTS product_field_blob;
DROP TABLE IF EXISTS product_field;
DROP TABLE IF EXISTS product;
