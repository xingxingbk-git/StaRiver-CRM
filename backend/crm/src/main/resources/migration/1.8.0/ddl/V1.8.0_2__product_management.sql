CREATE TABLE IF NOT EXISTS pm_product
(
    `id`                 VARCHAR(32)  NOT NULL COMMENT 'ID',
    `code`               VARCHAR(64)  NOT NULL COMMENT '产品代号',
    `name`               VARCHAR(255) NOT NULL COMMENT '产品名称',
    `version`            VARCHAR(64)  NOT NULL COMMENT '当前版本',
    `next_version`       VARCHAR(64) COMMENT '下一个版本',
    `status`             VARCHAR(32)  NOT NULL COMMENT '产品状态',
    `release_date`       VARCHAR(32) COMMENT '计划发布日期',
    `slogan`             VARCHAR(500) COMMENT '产品简介',
    `product_owner_id`   VARCHAR(32) COMMENT '产品负责人ID',
    `product_owner_name` VARCHAR(64) COMMENT '产品负责人',
    `dev_owner_id`       VARCHAR(32) COMMENT '研发负责人ID',
    `dev_owner_name`     VARCHAR(64) COMMENT '研发负责人',
    `organization_id`    VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `create_time`        BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`        BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`        VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`        VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) COMMENT = '产品管理-产品集'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_pm_product_org ON pm_product (`organization_id`);
CREATE UNIQUE INDEX uk_pm_product_org_code ON pm_product (`organization_id`, `code`);

CREATE TABLE IF NOT EXISTS pm_product_module
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'ID',
    `product_id`      VARCHAR(32)  NOT NULL COMMENT '产品ID',
    `parent_id`       VARCHAR(32) COMMENT '父级模块ID',
    `name`            VARCHAR(255) NOT NULL COMMENT '模块名称',
    `owner_id`        VARCHAR(32) COMMENT '负责人ID',
    `owner_name`      VARCHAR(64) COMMENT '负责人',
    `pending_count`   INT          NOT NULL DEFAULT 0 COMMENT '待发布需求数',
    `pos`             INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) COMMENT = '产品管理-模块架构'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_pm_module_product ON pm_product_module (`product_id`, `parent_id`, `pos`);
CREATE INDEX idx_pm_module_org ON pm_product_module (`organization_id`);

CREATE TABLE IF NOT EXISTS pm_product_version
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'ID',
    `product_id`      VARCHAR(32)  NOT NULL COMMENT '产品ID',
    `version`         VARCHAR(64)  NOT NULL COMMENT '版本号',
    `status`          VARCHAR(32)  NOT NULL COMMENT '状态',
    `release_date`    VARCHAR(32) COMMENT '发布日期',
    `description`     VARCHAR(1000) COMMENT '版本说明',
    `pending_count`   INT          NOT NULL DEFAULT 0 COMMENT '需求项数量',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) COMMENT = '产品管理-版本路线图'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_pm_version_product ON pm_product_version (`product_id`, `release_date`);
CREATE INDEX idx_pm_version_org ON pm_product_version (`organization_id`);

CREATE TABLE IF NOT EXISTS pm_product_requirement
(
    `id`                  VARCHAR(32)  NOT NULL COMMENT 'ID',
    `requirement_no`      VARCHAR(64)  NOT NULL COMMENT '需求编号',
    `title`               VARCHAR(255) NOT NULL COMMENT '需求标题',
    `type`                VARCHAR(32)  NOT NULL COMMENT '需求类型',
    `source`              VARCHAR(32)  NOT NULL COMMENT '需求来源',
    `product_id`          VARCHAR(32) COMMENT '目标产品ID',
    `product_name`        VARCHAR(255) COMMENT '目标产品',
    `target_version`      VARCHAR(64) COMMENT '预发布版本',
    `module_id`           VARCHAR(32) COMMENT '关联模块ID',
    `module_name`         VARCHAR(255) COMMENT '关联模块',
    `priority`            VARCHAR(16)  NOT NULL COMMENT '优先级',
    `status`              VARCHAR(32)  NOT NULL COMMENT '状态',
    `stage`               VARCHAR(32)  NOT NULL COMMENT '当前流程阶段',
    `expected_release`    VARCHAR(64) COMMENT '期望上线',
    `owner_id`            VARCHAR(32) COMMENT '负责人ID',
    `owner_name`          VARCHAR(64) COMMENT '负责人',
    `description`         TEXT COMMENT '需求描述',
    `acceptance_criteria` TEXT COMMENT '验收标准',
    `organization_id`     VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `create_time`         BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`         BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`         VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`         VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) COMMENT = '产品管理-产品需求'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE UNIQUE INDEX uk_pm_requirement_no ON pm_product_requirement (`requirement_no`);
CREATE INDEX idx_pm_requirement_org ON pm_product_requirement (`organization_id`);
CREATE INDEX idx_pm_requirement_product ON pm_product_requirement (`product_id`, `status`);

CREATE TABLE IF NOT EXISTS pm_product_document
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'ID',
    `product_id`      VARCHAR(32)  NOT NULL COMMENT '产品ID',
    `name`            VARCHAR(255) NOT NULL COMMENT '文档名称',
    `type`            VARCHAR(32) COMMENT '文档类型',
    `size_text`       VARCHAR(32) COMMENT '展示大小',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) COMMENT = '产品管理-产品文档'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_pm_document_product ON pm_product_document (`product_id`);
