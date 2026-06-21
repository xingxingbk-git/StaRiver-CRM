ALTER TABLE approval_flow
    ADD COLUMN `business_config` LONGTEXT COMMENT '业务流程扩展配置JSON';

ALTER TABLE pm_product_requirement
    ADD COLUMN `workflow_config_json` LONGTEXT COMMENT '交付流程配置快照JSON',
    ADD COLUMN `current_assignee_ids` LONGTEXT COMMENT '当前阶段负责人ID列表JSON',
    ADD COLUMN `current_assignee_names` VARCHAR(1000) COMMENT '当前阶段负责人名称',
    ADD COLUMN `target_version_id` VARCHAR(32) COMMENT '预发布版本ID';
