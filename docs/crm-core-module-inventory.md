# CRM 核心模块现状盘点表

更新时间：2026-06-17

## 1. 盘点结论

本项目是在 Cordys 开源代码基础上的二次开发 CRM 系统。当前前端、后端、权限、表单配置、审批、导入导出等基础能力已经具备，核心工作不应按从零开发处理，而应围绕现有模块做业务梳理、页面调整、接口联调、权限校验和部署准备。

当前代码结构可以支撑一条完整 CRM 销售链路：

线索管理 -> 客户管理 -> 商机管理 -> 报价管理 -> 合同管理 -> 回款/发票 -> 审批/消息/数据洞察。

下一步建议按模块逐个推进，优先顺序为：客户管理、线索管理、商机管理、报价管理、合同管理、产品集/产品需求、工作台/数据洞察、系统设置。

## 2. 总体架构现状

| 层级 | 现状 | 主要位置 | 备注 |
| --- | --- | --- | --- |
| 前端应用 | Vue 前端已按业务模块拆分，路由集中在 `router/routes/modules` | `frontend/packages/web` | 当前菜单、页面、权限标识主要由路由 meta 和后端权限共同控制 |
| 前端业务页面 | 客户、线索、商机、合同、产品、工作台、数据洞察、系统设置等页面目录已存在 | `frontend/packages/web/src/views` | 多数 CRM 页面已包含列表、视图、导出、详情抽屉或详情页能力 |
| 前端 API 聚合 | 大量接口从共享库 `@lib/shared/api/modules/*` 聚合导出，本项目另有产品管理本地接口 | `frontend/packages/web/src/api/modules/index.ts`、`frontend/packages/web/src/api/modules/productManagement.ts` | 后续联调要同时关注共享库接口定义和本项目本地接口 |
| 后端业务模块 | 后端按 CRM 业务域拆包，已有 controller/service/mapper/domain/dto 结构 | `backend/crm/src/main/java/cn/cordys/crm` | 客户、线索、商机、合同、产品、仪表盘、系统、审批等模块均存在 |
| 数据迁移 | 数据库迁移脚本按版本维护，最新可见 `1.8.0` 产品管理相关脚本 | `backend/crm/src/main/resources/migration` | 后续新增字段或字典时应优先通过迁移脚本记录 |
| 系统支撑 | 登录、组织、用户、角色、菜单模块、消息、审批、日志、附件等 controller 已存在 | `backend/crm/src/main/java/cn/cordys/crm/system`、`backend/crm/src/main/java/cn/cordys/crm/approval` | 核心业务联调前需要确认权限和审批配置是否完整 |

## 3. 核心模块盘点

### 3.1 客户管理

| 项目 | 现状 |
| --- | --- |
| 业务定位 | CRM 主数据模块，承接线索转化、商机关联、合同关联、回款/发票查看 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/customer.ts` |
| 前端页面 | `frontend/packages/web/src/views/customer/customer.vue`、`contact.vue`、`openSea.vue`、`customerDetail.vue` |
| 前端组件 | `customerTable.vue`、`openSeaTable.vue`、`customerOverviewDrawer.vue`、`openSeaOverviewDrawer.vue`、`customerRelation.vue`、`collaborator.vue`、`mergeAccountModal.vue` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/customer` |
| 后端接口入口 | `CustomerController`、`CustomerContactController`、`CustomerPoolController`、`PoolCustomerController`、`CustomerRelationController`、`CustomerCollaborationController`、`CustomerFollowPlanController`、`CustomerFollowRecordController`、`CustomerOwnerHistoryController`、`CustomerCapacityController` |
| 现有能力 | 客户列表、客户详情、联系人、公海、客户池、领取/分配/转移、协作人、客户关系、跟进计划、跟进记录、导入导出、统计、与合同/回款/发票/订单关联 |
| 权限标识 | `CUSTOMER_MANAGEMENT:READ`、`CUSTOMER_MANAGEMENT_POOL:READ`、`CUSTOMER_MANAGEMENT_CONTACT:READ` 等 |
| 待核查点 | 客户字段是否符合当前业务；客户详情关联数据是否完整；公海领取/分配权限是否正确；导入导出模板是否仍适用；客户与线索/商机/合同的关联口径是否一致 |
| 建议下一步 | 作为第一优先模块做页面实测和接口联调，产出字段清单、接口清单、权限矩阵和问题清单 |

### 3.2 线索管理

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 销售线索入口，负责线索沉淀、线索池管理和向客户/商机转化 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/clue.ts` |
| 前端页面 | `frontend/packages/web/src/views/clueManagement/clue/index.vue`、`frontend/packages/web/src/views/clueManagement/cluePool/index.vue` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/clue` |
| 后端接口入口 | `ClueController`、`CluePoolController`、`PoolClueController`、`ClueCapacityController`、`ClueFollowPlanController`、`ClueFollowRecordController`、`ClueOwnerHistoryController`、`ClueUserViewController`、`PoolClueUserViewController` |
| 现有能力 | 线索列表、线索池、领取、分配、转移、移入线索池、状态更新、线索转客户、转商机、跟进计划、跟进记录、视图配置、导入导出、图表统计 |
| 权限标识 | `CLUE_MANAGEMENT:READ`、`CLUE_MANAGEMENT_POOL:READ` 等 |
| 待核查点 | 线索来源、状态、负责人、转化规则是否满足业务；转客户和转商机时字段映射是否正确；线索池容量/回收规则是否启用；重复线索处理是否清晰 |
| 建议下一步 | 在客户模块稳定后，重点验证线索转客户、线索转商机两条路径 |

### 3.3 商机管理

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 销售过程核心，承接客户关系、销售阶段、报价和合同 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/opportunity.ts` |
| 前端页面 | `frontend/packages/web/src/views/opportunity/index.vue`、`frontend/packages/web/src/views/opportunity/components/opportunityTable.vue`、`frontend/packages/web/src/views/opportunity/components/optOverviewDrawer.vue` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/opportunity` |
| 后端接口入口 | `OpportunityController`、`OpportunityStageController`、`OpportunityRuleController`、`OpportunityFollowPlanController`、`OpportunityFollowRecordController`、`OpportunityUserViewController` |
| 现有能力 | 商机列表、商机详情、阶段配置、阶段推进/回滚、排序、转移、批量更新、跟进计划、跟进记录、联系人列表、导入导出、统计图表 |
| 权限标识 | `OPPORTUNITY_MANAGEMENT:READ`、`OPPORTUNITY_QUOTATION:READ` 等 |
| 待核查点 | 商机阶段是否符合当前销售流程；赢单/输单/关闭规则是否明确；商机关联客户、联系人、产品、报价、合同的链路是否完整；阶段回滚权限是否需要限制 |
| 建议下一步 | 与报价模块一起联调，先确保商机阶段和商机详情稳定 |

### 3.4 报价管理

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 商机成交前的报价单管理，连接产品价格和合同 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/opportunity.ts` 中 `/opportunity/quotation` |
| 前端页面 | `frontend/packages/web/src/views/opportunity/quotation.vue`、`frontend/packages/web/src/views/opportunity/components/quotation/*` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/opportunity` |
| 后端接口入口 | `OpportunityQuotationController`、`OpportunityQuotationUserViewController` |
| 现有能力 | 报价列表、报价新增/编辑、详情、快照详情、表单配置、审批、撤回、作废、批量审批、批量作废、视图配置、下载/PDF 导出 |
| 权限标识 | `OPPORTUNITY_QUOTATION:READ` 等，审批权限需结合审批模块核查 |
| 待核查点 | 报价产品明细、价格、折扣、税率、总金额计算是否符合业务；报价快照是否能防止历史数据被产品改价影响；报价审批状态和作废状态是否互斥清晰 |
| 建议下一步 | 商机稳定后联调报价，重点看报价表单、金额计算、审批和快照 |

### 3.5 合同管理

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 销售成交后的合同、回款、发票和业务名称管理 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/contract.ts` |
| 前端页面 | `contract/contract/index.vue`、`contractPaymentPlan/index.vue`、`contractPaymentRecord/index.vue`、`invoice/index.vue`、`businessTitle/index.vue` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/contract` |
| 后端接口入口 | `ContractController`、`ContractStageController`、`ContractPaymentPlanController`、`ContractPaymentRecordController`、`ContractInvoiceController`、`BusinessTitleController`、`BusinessTitleConfigController` |
| 现有能力 | 合同列表、合同详情、合同审批/撤回、合同状态变更、合同阶段配置、回款计划、回款记录、发票、合同业务名称、视图配置、导入导出、统计 |
| 权限标识 | `CONTRACT:READ`、`CONTRACT_PAYMENT_PLAN:READ`、`CONTRACT_PAYMENT_RECORD:READ`、`CONTRACT_INVOICE:READ`、`CONTRACT_BUSINESS_TITLE:READ` 等 |
| 待核查点 | 合同与报价/商机/客户的关联是否完整；合同金额、回款计划、回款记录、发票金额之间是否有一致性校验；合同审批流程是否启用；合同阶段与合同状态是否重复或冲突 |
| 建议下一步 | 报价稳定后推进合同，优先验证合同新增、详情、回款计划、回款记录、发票 |

### 3.6 产品集与产品需求管理

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 产品基础资料、产品价格和需求管理，为报价和商机提供产品来源 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/product.ts` |
| 前端页面 | `frontend/packages/web/src/views/product/index.vue`、`create.vue`、`detail.vue`、`price.vue`、`requirements.vue` |
| 前端 API | 共享产品接口聚合在 `frontend/packages/web/src/api/modules/index.ts`；产品需求本地接口在 `frontend/packages/web/src/api/modules/productManagement.ts` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/product`、`backend/crm/src/main/java/cn/cordys/crm/productmgmt` |
| 后端接口入口 | `ProductController`、`ProductPriceController`、`ProductManagementController` |
| 数据迁移 | `backend/crm/src/main/resources/migration/1.8.0/ddl/V1.8.0_2__product_management.sql`、`dml/V1.8.0_2_1__product_management_data.sql` |
| 现有能力 | 产品列表、产品详情、新增/编辑、产品价格、产品需求列表/详情/新增、产品路线图 |
| 权限标识 | `PRODUCT_MANAGEMENT:READ`、`PRODUCT_MANAGEMENT:ADD`、`PRICE:READ` 等 |
| 待核查点 | 产品管理和产品需求接口风格是否统一；产品价格是否能被报价正确引用；产品改价后历史报价是否使用快照；需求管理是否需要审批或状态流转 |
| 建议下一步 | 在报价联调前核查产品和价格数据，避免报价模块反复返工 |

### 3.7 工作台

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 内部用户进入系统后的工作入口，展示待办、快捷入口和核心经营概览 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/workbench.ts` |
| 前端页面 | `frontend/packages/web/src/views/workbench/index.vue`、`components/overview.vue`、`dataOverviewIndex.vue`、`quickAccess.vue`、`categoryCard.vue` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/home` |
| 后端接口入口 | `HomeStatisticController` |
| 现有能力 | 部门树、线索统计、进行中商机统计、成功商机统计、商机跟进等工作台统计接口 |
| 待核查点 | 指标口径是否和业务模块一致；待办来源是否需要接入审批、跟进计划、消息；快捷入口是否按角色展示 |
| 建议下一步 | 核心业务链路稳定后再统一校准工作台指标 |

### 3.8 数据洞察

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 经营数据看板和自定义 dashboard |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/dashboard.ts` |
| 前端页面 | `frontend/packages/web/src/views/dashboard/index.vue`、`module.vue`、`fullPage.vue`、`link.vue`、`components/dashboard.vue` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/dashboard` |
| 后端接口入口 | `DashboardController`、`DashboardModuleController` |
| 现有能力 | Dashboard 新增、重命名、收藏、拖拽、模块树、模块数量、全屏/链接页面等 |
| 待核查点 | 当前 dashboard 是偏配置化看板还是固定 CRM 指标看板；权限数据范围是否跟业务模块一致；图表数据口径是否可追溯到明细 |
| 建议下一步 | 等客户、线索、商机、合同数据口径明确后再补数据洞察 |

### 3.9 系统设置与权限支撑

| 项目 | 现状 |
| --- | --- |
| 业务定位 | 用户、角色、组织、菜单模块、消息、审批流程、企业设置、系统日志、字典等基础支撑 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules/system.ts` |
| 前端页面 | `system/org/index.vue`、`role/index.vue`、`module/index.vue`、`message/index.vue`、`process/*`、`business/index.vue`、`log/index.vue` |
| 后端模块 | `backend/crm/src/main/java/cn/cordys/crm/system`、`backend/crm/src/main/java/cn/cordys/crm/approval` |
| 后端接口入口 | `OrganizationUserController`、`DepartmentController`、`RoleController`、`ModuleController`、`ModuleFieldController`、`ModuleFormController`、`MessageTaskController`、`AnnouncementController`、`OperationLogController`、`DictController`、`ApprovalFlowController`、`ApprovalActionController`、`ApprovalTodoController`、`ApprovalResourceController` |
| 现有能力 | 用户管理、部门组织、角色权限、菜单/模块配置、字段/表单配置、消息公告、审批流程、审批待办、操作日志、系统字典、附件、登录日志等 |
| 待核查点 | 角色权限是否同时控制菜单、按钮和接口；审批流程是否能覆盖报价/合同/发票等业务；系统字典是否覆盖客户来源、线索状态、商机阶段、合同状态；日志是否记录关键业务操作 |
| 建议下一步 | 业务模块联调时同步补权限矩阵，避免最后统一补权限成本过高 |

## 4. 关键依赖关系

| 上游模块 | 下游模块 | 依赖内容 | 联调重点 |
| --- | --- | --- | --- |
| 产品集/产品价格 | 报价管理 | 产品、规格、价格、折扣、税率、金额 | 报价是否保存快照，产品改价是否影响历史报价 |
| 线索管理 | 客户管理 | 线索转客户 | 字段映射、重复客户校验、负责人归属 |
| 线索管理 | 商机管理 | 线索转商机 | 客户/联系人/来源字段是否带入 |
| 客户管理 | 商机管理 | 客户下创建或关联商机 | 客户详情能否展示关联商机 |
| 商机管理 | 报价管理 | 商机下生成报价 | 报价是否回写商机，报价状态是否影响商机阶段 |
| 报价管理 | 合同管理 | 报价生成或关联合同 | 金额、产品明细、客户、商机是否一致 |
| 合同管理 | 回款/发票 | 合同金额、回款计划、回款记录、发票 | 金额一致性、状态流转、审批状态 |
| 审批流程 | 报价/合同/发票 | 审批流配置、待办、撤回、驳回、通过 | 审批状态与业务状态是否清晰 |
| 角色权限 | 全部业务模块 | 菜单、按钮、接口、数据范围 | 前端隐藏和后端拒绝要一致 |
| 系统字典 | 全部业务模块 | 状态、来源、类型、原因等枚举 | 字典值、前端展示、后端校验一致 |

## 5. 近期开发优先级

| 优先级 | 模块 | 目标 | 交付物 |
| --- | --- | --- | --- |
| P0 | 客户管理 | 先稳定 CRM 主数据 | 字段清单、接口清单、权限矩阵、页面问题清单、客户详情联调结果 |
| P0 | 线索管理 | 跑通线索转化 | 线索转客户/商机映射表、线索池规则、状态流转说明 |
| P0 | 商机管理 | 稳定销售过程 | 商机阶段配置、阶段推进规则、商机详情关联数据 |
| P0 | 报价管理 | 跑通报价闭环 | 报价字段、金额计算规则、审批/作废规则、快照验证 |
| P0 | 合同管理 | 跑通成交与回款 | 合同字段、回款计划、回款记录、发票、审批状态规则 |
| P1 | 产品集/产品需求 | 支撑报价和产品管理 | 产品/价格字段、需求字段、产品价格快照规则 |
| P1 | 角色权限/审批/字典 | 支撑业务模块上线 | 权限矩阵、审批配置、字典清单 |
| P2 | 工作台/数据洞察 | 做经营展示 | 指标口径、图表清单、数据范围规则 |

## 6. 模块推进模板

每个模块建议按同一个模板推进，避免边改边散：

1. 现状确认：路由、页面、接口、数据库表、权限标识。
2. 字段确认：列表字段、筛选字段、表单字段、详情字段、导入导出字段。
3. 接口确认：分页、详情、新增、编辑、删除、批量、导入导出、状态流转、审批。
4. 权限确认：菜单权限、按钮权限、接口权限、数据范围权限。
5. 联调验证：正常流、异常流、权限不足、空数据、重复数据、导入导出。
6. 问题记录：页面问题、接口问题、数据问题、权限问题、部署问题。
7. 验收归档：截图、接口样例、测试账号、已知限制、后续优化项。

## 7. 第一轮建议执行项

第一轮建议从客户管理开始，原因是客户是线索、商机、报价、合同的共同主数据。

| 序号 | 动作 | 目标 |
| --- | --- | --- |
| 1 | 打开 `/account/index`、联系人、公海、客户详情 | 确认客户模块页面是否能正常进入 |
| 2 | 梳理客户列表、表单、详情字段 | 形成客户字段清单 |
| 3 | 梳理 `customerApi` 导出的客户相关接口 | 形成客户接口清单 |
| 4 | 对照 `backend/crm/src/main/java/cn/cordys/crm/customer/controller` | 确认前后端接口覆盖关系 |
| 5 | 验证新增、编辑、详情、联系人、公海领取、转移、导出 | 形成第一版客户模块问题清单 |
| 6 | 补客户模块权限矩阵 | 明确菜单、按钮、接口、数据范围控制 |

