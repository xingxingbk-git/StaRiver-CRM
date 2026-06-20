INSERT IGNORE INTO pm_product
(`id`, `code`, `name`, `version`, `next_version`, `status`, `release_date`, `slogan`, `product_owner_id`, `product_owner_name`,
 `dev_owner_id`, `dev_owner_name`, `organization_id`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
('p1', 'STARIVER', 'StaRiver AI 中台', 'v3.8.2', 'v4.0', '已发布', '2026-05-20',
 '面向工业场景的 AI 基础设施平台，提供数据接入、特征管理、模型训练与推理编排', 'admin', 'Administrator', 'admin', 'Administrator',
 '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('p2', 'OPTIQA', 'OptiQA 智能质检', 'v2.1.0', 'v2.2', '开发中', '2026-09-20',
 '基于视觉识别的工业质检平台，支持缺陷检测、良率分析与产线实时监控', 'admin', 'Administrator', 'admin', 'Administrator',
 '100001', UNIX_TIMESTAMP() * 1000 + 1, UNIX_TIMESTAMP() * 1000 + 1, 'admin', 'admin'),
('p3', 'DATEX', 'DateXone', 'v1.0', 'v1.1', '规划中', '2026-10-30',
 'DateXone产品简介', 'admin', 'Administrator', 'admin', 'Administrator',
 '100001', UNIX_TIMESTAMP() * 1000 + 2, UNIX_TIMESTAMP() * 1000 + 2, 'admin', 'admin');

INSERT IGNORE INTO pm_product_module
(`id`, `product_id`, `parent_id`, `name`, `owner_id`, `owner_name`, `pending_count`, `pos`, `organization_id`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
('pm1', 'p1', NULL, '数据接入', 'admin', 'Administrator', 3, 1, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pm1-1', 'p1', 'pm1', '设备采集', 'admin', 'Administrator', 1, 1, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pm1-2', 'p1', 'pm1', '文件导入', 'admin', 'Administrator', 1, 2, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pm1-3', 'p1', 'pm1', '接口同步', 'admin', 'Administrator', 1, 3, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pm2', 'p1', NULL, '模型训练', 'admin', 'Administrator', 5, 2, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pm2-1', 'p1', 'pm2', '训练任务', 'admin', 'Administrator', 3, 1, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pm2-2', 'p1', 'pm2', '评估中心', 'admin', 'Administrator', 2, 2, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin');

INSERT IGNORE INTO pm_product_version
(`id`, `product_id`, `version`, `status`, `release_date`, `description`, `pending_count`, `organization_id`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
('pv1', 'p1', 'v4.1', '规划中', '2026-12-15', '[新功能] 智能运维模块、自定义 SLA 策略；[修复] 权限与消息通知体验优化', 8, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pv2', 'p1', 'v4.0', '开发中', '2026-09-30', '[新功能] BI 分析、API 开放平台、告警引擎 Flink 实时化；[修复] 自定义 SLA 策略', 25, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pv3', 'p1', 'v3.8.2', '已上线', '2026-05-20', '[新功能] 数据接入升级、告警规则引擎优化、移动端扫码录入；[修复] 自定义 SLA 策略', 12, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pv4', 'p2', 'v2.2', '规划中', '2026-10-30', '[新功能] 缺陷模型自学习与产线看板增强', 6, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pv5', 'p2', 'v2.1.0', '开发中', '2026-09-20', '[新功能] 质检策略编排和异常样本管理', 5, '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin');

INSERT IGNORE INTO pm_product_requirement
(`id`, `requirement_no`, `title`, `type`, `source`, `product_id`, `product_name`, `target_version`, `module_id`, `module_name`,
 `priority`, `status`, `stage`, `expected_release`, `owner_id`, `owner_name`, `description`, `acceptance_criteria`,
 `organization_id`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
('pr1', 'PRM-2026-0106-01', 'BI 看板支持多维钻取与自定义指标', '功能新增', '客户', 'p1', 'StaRiver', 'v4.0', 'pm2', '模型训练',
 'P1', '需求评估', '需求评审', '2026-Q3', 'admin', 'Administrator',
 '作为 BI 分析师，我希望在任意维度钻取并查看同比环比，以便减少跨表排查时间、在客户会议上快速响应质疑。',
 'Given 前置条件：用户已登录并有看板权限\nWhen 操作动作：用户选择维度并钻取\nThen 预期结果：系统展示同比环比数据',
 '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pr2', 'PRM-2026-0148-01', '告警引擎支持 Flink 实时处理', '性能改进', '内部', 'p1', 'StaRiver', 'v4.0', 'pm1', '数据接入',
 'P0', '开发中', '开发', '2026-Q3', 'admin', 'Administrator', '实时告警链路支持 Flink 处理。', '延迟低于 5 秒。',
 '100001', UNIX_TIMESTAMP() * 1000 + 1, UNIX_TIMESTAMP() * 1000 + 1, 'admin', 'admin'),
('pr3', 'PRM-2026-0110-01', 'FinGuard 合规报告自动生成（GB/T 40660）', '功能新增', '内部', 'p2', 'OptiQA', 'v2.2', NULL, NULL,
 'P0', '待发布', '产品验收', '2026-Q4', 'admin', 'Administrator', '合规报告自动生成。', '完成报告导出和格式校验。',
 '100001', UNIX_TIMESTAMP() * 1000 + 2, UNIX_TIMESTAMP() * 1000 + 2, 'admin', 'admin');

INSERT IGNORE INTO pm_product_document
(`id`, `product_id`, `name`, `type`, `size_text`, `organization_id`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
('pd1', 'p1', 'StaRiver 产品白皮书 v3.8.2.pdf', '白皮书', '3.8 MB', '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pd2', 'p1', 'StaRiver 功能手册.pdf', '手册', '6.2 MB', '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin'),
('pd3', 'p1', 'StaRiver API 接口文档.pdf', '接口', '2.4 MB', '100001', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin');
