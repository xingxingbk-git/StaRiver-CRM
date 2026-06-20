ALTER TABLE pm_product_version
    MODIFY COLUMN description MEDIUMTEXT NULL COMMENT '版本说明',
    ADD COLUMN product_owner_id VARCHAR(32) NULL COMMENT '产品负责人ID' AFTER pending_count,
    ADD COLUMN product_owner_name VARCHAR(64) NULL COMMENT '产品负责人' AFTER product_owner_id,
    ADD COLUMN dev_owner_id VARCHAR(32) NULL COMMENT '研发负责人ID' AFTER product_owner_name,
    ADD COLUMN dev_owner_name VARCHAR(64) NULL COMMENT '研发负责人' AFTER dev_owner_id,
    ADD COLUMN attachment_ids VARCHAR(1000) NULL COMMENT '附件ID集合' AFTER dev_owner_name;

UPDATE pm_product_version pv
JOIN pm_product p ON p.id = pv.product_id
SET pv.product_owner_id = p.product_owner_id,
    pv.product_owner_name = p.product_owner_name,
    pv.dev_owner_id = p.dev_owner_id,
    pv.dev_owner_name = p.dev_owner_name
WHERE pv.product_owner_id IS NULL
   OR pv.dev_owner_id IS NULL;

UPDATE pm_product
SET product_owner_name = 'Administrator',
    dev_owner_name = 'Administrator'
WHERE organization_id = '100001'
  AND (product_owner_id = 'admin' OR dev_owner_id = 'admin');

UPDATE pm_product_module
SET owner_name = 'Administrator'
WHERE organization_id = '100001'
  AND owner_id = 'admin';

UPDATE pm_product_requirement
SET owner_name = 'Administrator'
WHERE organization_id = '100001'
  AND owner_id = 'admin';

UPDATE pm_product_version
SET product_owner_name = 'Administrator',
    dev_owner_name = 'Administrator'
WHERE organization_id = '100001'
  AND (product_owner_id = 'admin' OR dev_owner_id = 'admin');
