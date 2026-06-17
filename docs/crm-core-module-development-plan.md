# CRM 核心模块开发计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 Cordys 二次开发代码基础上，按模块逐步完成 CRM 核心销售链路的业务梳理、页面调整、接口联调、权限校验和上线准备。

**Architecture:** 前端以 `frontend/packages/web` 的 Vue 业务模块为入口，沿路由、页面、组件、API 聚合层向后端 controller/service/mapper 追踪。后端以 `backend/crm` 的业务域包为边界推进，每个模块只在当前业务边界内收敛需求、修复联调问题和补齐必要配置，避免一次性大重构。

**Tech Stack:** Vue 3、Vite、TypeScript、Naive UI、Spring Boot、Maven、MyBatis、Flyway、MySQL、Redis、Shiro。

---

## 1. 计划范围

本计划覆盖 CRM 核心模块的后续开发推进，不覆盖从零重建框架。

纳入范围：

- 客户管理
- 线索管理
- 商机管理
- 报价管理
- 合同管理
- 产品集与产品需求管理
- 工作台与数据洞察
- 用户、角色权限、审批、消息、企业设置、系统日志、系统字典等支撑模块
- 本地联调、验收、部署准备

暂不纳入范围：

- 更换前端框架或 UI 组件库
- 大规模重写后端分层架构
- 脱离现有数据库迁移体系直接改库
- 未经确认的新业务模块扩展

## 2. 文档与代码入口

| 类型 | 路径 | 用途 |
| --- | --- | --- |
| 现状盘点表 | `docs/crm-core-module-inventory.md` | 当前模块现状、依赖关系、优先级 |
| 本开发计划 | `docs/crm-core-module-development-plan.md` | 阶段计划、任务拆分、验收标准 |
| 前端主工程 | `frontend/packages/web` | CRM Web 页面、路由、API 聚合、构建脚本 |
| 前端路由 | `frontend/packages/web/src/router/routes/modules` | 菜单、模块路由、权限标识 |
| 前端页面 | `frontend/packages/web/src/views` | 各业务模块页面与组件 |
| 前端 API 聚合 | `frontend/packages/web/src/api/modules/index.ts` | 共享 API 导出入口 |
| 产品需求本地 API | `frontend/packages/web/src/api/modules/productManagement.ts` | 产品需求和产品管理本地接口 |
| 后端 CRM 模块 | `backend/crm/src/main/java/cn/cordys/crm` | CRM 业务 controller/service/domain/mapper |
| 数据迁移 | `backend/crm/src/main/resources/migration` | Flyway 版本化 DDL/DML |
| 后端启动模块 | `backend/app` | Spring Boot 应用入口与打包模块 |

## 3. 开发原则

- 先跑通链路，再优化体验。
- 先客户，再线索、商机、报价、合同，保持模块推进顺序稳定。
- 每个模块都要产出字段清单、接口清单、权限矩阵、问题清单和验收记录。
- 页面调整优先复用现有组件和业务模式。
- 后端改动优先落在当前业务域包内，必要的公共能力再进入 `common` 或系统模块。
- 数据库变化必须进入 `backend/crm/src/main/resources/migration`。
- 权限必须同时校验菜单、按钮、接口和数据范围。
- 每一阶段完成后都要跑前端类型检查、前端构建和后端 Maven 验证。

## 4. 验证命令

后续每个阶段至少使用以下命令做基础验证。

前端类型检查：

```bash
cd frontend/packages/web
pnpm run type:check
```

前端构建：

```bash
cd frontend/packages/web
pnpm run build:local
```

后端编译测试：

```bash
cd backend
mvn test
```

后端打包：

```bash
cd backend
mvn clean package -DskipTests
```

本地页面验证建议覆盖：

- `/account/index`
- `/lead/index`
- `/lead/leadPool`
- `/opportunity/opt`
- `/opportunity/quotation`
- `/contract/index`
- `/contract/contractPaymentPlan`
- `/contract/contractPaymentRecord`
- `/contract/contractInvoice`
- `/product/pro`
- `/product/requirements`
- `/system/role`
- `/system/process/process/process`

## 5. 阶段计划

### 阶段 0：开发基线与联调环境确认

**目标：** 确认本地前后端、数据库、Redis、权限、Mock 配置和基础账号可用，为后续模块联调建立稳定基线。

**主要文件：**

- `frontend/packages/web/config/plugin/mock.ts`
- `frontend/packages/web/src/router/guard/userLoginInfo.ts`
- `frontend/packages/web/src/router/routes/modules/*.ts`
- `backend/app`
- `backend/crm/src/main/resources/migration`
- `installer`

**任务：**

- [ ] 确认前端本地启动命令、访问地址和代理配置。
- [ ] 确认后端启动方式、端口、数据库连接、Redis 连接。
- [ ] 确认 Mock 是否影响登录、接口和静态资源响应。
- [ ] 确认默认账号、角色、权限、菜单能进入核心 CRM 页面。
- [ ] 整理本地开发启动说明，追加到项目文档。

**验收标准：**

- 前端能进入登录页和主页面。
- 后端能正常启动并完成迁移。
- 客户、线索、商机、报价、合同、产品、系统设置菜单可见性符合当前账号权限。
- 明确哪些接口走真实后端，哪些仍受 Mock 或共享库影响。

### 阶段 1：客户管理模块开发

**目标：** 稳定客户主数据模块，作为后续线索、商机、报价、合同联调的基础。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/customer.ts`
- `frontend/packages/web/src/views/customer/customer.vue`
- `frontend/packages/web/src/views/customer/customerDetail.vue`
- `frontend/packages/web/src/views/customer/contact.vue`
- `frontend/packages/web/src/views/customer/openSea.vue`
- `frontend/packages/web/src/views/customer/components/customerTable.vue`
- `frontend/packages/web/src/views/customer/components/openSeaTable.vue`
- `frontend/packages/web/src/api/modules/index.ts`
- `backend/crm/src/main/java/cn/cordys/crm/customer`

**任务：**

- [ ] 梳理客户列表字段、筛选字段、表单字段、详情字段。
- [ ] 梳理联系人、公海、客户池、协作人、客户关系的页面入口。
- [ ] 对照 `customerApi` 导出和 `customer/controller`，形成客户接口清单。
- [ ] 验证客户列表分页、筛选、排序、导出。
- [ ] 验证客户新增、编辑、删除、批量更新。
- [ ] 验证客户详情中的联系人、商机、合同、回款、发票、订单关联数据。
- [ ] 验证公海领取、分配、转移、回收相关规则。
- [ ] 补客户模块权限矩阵。
- [ ] 修复客户模块联调发现的页面、接口、字段、权限问题。

**验收标准：**

- 客户列表、详情、联系人、公海均可正常使用。
- 客户新增/编辑字段与后端 DTO、数据库字段一致。
- 客户详情能承载后续商机、合同、回款、发票等关联信息。
- 权限不足时前端按钮隐藏和后端接口拒绝一致。

### 阶段 2：线索管理模块开发

**目标：** 跑通线索从录入、分配、跟进到转客户/转商机的完整流程。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/clue.ts`
- `frontend/packages/web/src/views/clueManagement/clue/index.vue`
- `frontend/packages/web/src/views/clueManagement/cluePool/index.vue`
- `frontend/packages/web/src/api/modules/index.ts`
- `backend/crm/src/main/java/cn/cordys/crm/clue`

**任务：**

- [ ] 梳理线索字段、线索池字段、状态字段和来源字段。
- [ ] 梳理线索列表、线索池、跟进计划、跟进记录页面入口。
- [ ] 对照 `clueApi` 导出和 `clue/controller`，形成线索接口清单。
- [ ] 验证线索新增、编辑、删除、导入、导出。
- [ ] 验证线索分配、转移、领取、移入线索池。
- [ ] 验证线索转客户字段映射。
- [ ] 验证线索转商机字段映射。
- [ ] 验证重复线索、重复客户校验规则。
- [ ] 补线索模块权限矩阵。
- [ ] 修复线索模块联调发现的问题。

**验收标准：**

- 线索和线索池页面稳定。
- 线索可以正确转为客户。
- 线索可以正确转为商机。
- 转化后的负责人、客户、联系人、来源、状态关系清晰。

### 阶段 3：产品集、产品价格与产品需求开发

**目标：** 在报价模块前先稳定产品与价格来源，避免报价金额和历史快照反复返工。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/product.ts`
- `frontend/packages/web/src/views/product/index.vue`
- `frontend/packages/web/src/views/product/create.vue`
- `frontend/packages/web/src/views/product/detail.vue`
- `frontend/packages/web/src/views/product/price.vue`
- `frontend/packages/web/src/views/product/requirements.vue`
- `frontend/packages/web/src/api/modules/index.ts`
- `frontend/packages/web/src/api/modules/productManagement.ts`
- `backend/crm/src/main/java/cn/cordys/crm/product`
- `backend/crm/src/main/java/cn/cordys/crm/productmgmt`
- `backend/crm/src/main/resources/migration/1.8.0`

**任务：**

- [ ] 梳理产品字段、产品模块字段、产品价格字段、产品需求字段。
- [ ] 确认共享产品接口和本地产品管理接口的职责边界。
- [ ] 验证产品列表、新增、编辑、详情。
- [ ] 验证产品价格列表、新增、编辑、复制、导入、导出。
- [ ] 验证产品需求列表、详情、新增。
- [ ] 明确报价引用产品时需要使用的字段和价格规则。
- [ ] 明确产品改价后历史报价是否必须使用快照数据。
- [ ] 补产品模块权限矩阵。
- [ ] 修复产品模块联调发现的问题。

**验收标准：**

- 产品和价格可以被报价模块稳定引用。
- 产品需求管理能完成当前业务需要的基础记录。
- 产品价格变更不会破坏历史报价口径。

### 阶段 4：商机管理模块开发

**目标：** 稳定销售过程管理，形成客户到报价、合同前的核心业务中台。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/opportunity.ts`
- `frontend/packages/web/src/views/opportunity/index.vue`
- `frontend/packages/web/src/views/opportunity/components/opportunityTable.vue`
- `frontend/packages/web/src/views/opportunity/components/optOverviewDrawer.vue`
- `frontend/packages/web/src/api/modules/index.ts`
- `backend/crm/src/main/java/cn/cordys/crm/opportunity`

**任务：**

- [ ] 梳理商机字段、阶段字段、失败原因、预计成交金额、预计成交时间。
- [ ] 梳理商机列表、详情抽屉、阶段配置、跟进计划、跟进记录。
- [ ] 对照 `opportunityApi` 导出和 `opportunity/controller`，形成商机接口清单。
- [ ] 验证商机新增、编辑、删除、批量更新、导入、导出。
- [ ] 验证商机阶段推进、回滚、排序。
- [ ] 验证商机关联客户、联系人、产品、报价、合同。
- [ ] 明确赢单、输单、关闭规则。
- [ ] 补商机模块权限矩阵。
- [ ] 修复商机模块联调发现的问题。

**验收标准：**

- 商机列表和详情稳定。
- 商机阶段可以按业务规则推进。
- 商机能正确承接客户和产品，并能作为报价入口。

### 阶段 5：报价管理模块开发

**目标：** 跑通报价新增、编辑、审批、作废、快照和导出流程。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/opportunity.ts`
- `frontend/packages/web/src/views/opportunity/quotation.vue`
- `frontend/packages/web/src/views/opportunity/components/quotation/*`
- `frontend/packages/web/src/api/modules/index.ts`
- `backend/crm/src/main/java/cn/cordys/crm/opportunity/controller/OpportunityQuotationController.java`
- `backend/crm/src/main/java/cn/cordys/crm/opportunity/controller/OpportunityQuotationUserViewController.java`

**任务：**

- [ ] 梳理报价字段、报价产品明细、金额字段、审批字段、作废字段。
- [ ] 梳理报价列表、详情、快照详情、PDF 导出页面。
- [ ] 验证报价新增、编辑、删除、详情。
- [ ] 验证报价从商机进入时自动带入客户、联系人、商机、产品信息。
- [ ] 验证报价金额计算规则：单价、数量、折扣、税率、总额。
- [ ] 验证报价审批、撤回、作废、批量审批、批量作废。
- [ ] 验证报价快照不会被产品价格变化影响。
- [ ] 补报价模块权限矩阵。
- [ ] 修复报价模块联调发现的问题。

**验收标准：**

- 报价可以从商机链路生成并保存。
- 报价金额前后端一致。
- 审批、撤回、作废状态清晰且互斥。
- 报价快照和导出可用于业务留痕。

### 阶段 6：合同、回款、发票模块开发

**目标：** 跑通销售成交后的合同签订、回款计划、回款记录和发票管理。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/contract.ts`
- `frontend/packages/web/src/views/contract/contract/index.vue`
- `frontend/packages/web/src/views/contract/contractPaymentPlan/index.vue`
- `frontend/packages/web/src/views/contract/contractPaymentRecord/index.vue`
- `frontend/packages/web/src/views/contract/invoice/index.vue`
- `frontend/packages/web/src/views/contract/businessTitle/index.vue`
- `frontend/packages/web/src/api/modules/index.ts`
- `backend/crm/src/main/java/cn/cordys/crm/contract`

**任务：**

- [ ] 梳理合同字段、合同状态、合同阶段、合同审批字段。
- [ ] 梳理回款计划、回款记录、发票、合同业务名称字段。
- [ ] 对照 `contractApi` 导出和 `contract/controller`，形成合同接口清单。
- [ ] 验证合同新增、编辑、详情、删除、状态变更。
- [ ] 验证合同关联客户、商机、报价。
- [ ] 验证回款计划新增、编辑、删除、统计。
- [ ] 验证回款记录新增、编辑、删除、导入、导出。
- [ ] 验证发票新增、编辑、审批、撤回、导出。
- [ ] 验证合同金额、回款计划、回款记录、发票金额一致性。
- [ ] 补合同模块权限矩阵。
- [ ] 修复合同模块联调发现的问题。

**验收标准：**

- 合同可以承接报价/商机/客户。
- 回款计划、回款记录、发票之间的数据关系清晰。
- 合同审批和发票审批能稳定运行。
- 客户详情能看到合同、回款、发票关联数据。

### 阶段 7：系统设置、权限、审批、消息与字典

**目标：** 补齐核心业务上线所需的系统支撑能力，保证菜单、按钮、接口、审批和字典配置可控。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/system.ts`
- `frontend/packages/web/src/views/system/org/index.vue`
- `frontend/packages/web/src/views/system/role/index.vue`
- `frontend/packages/web/src/views/system/module/index.vue`
- `frontend/packages/web/src/views/system/message/index.vue`
- `frontend/packages/web/src/views/system/process`
- `frontend/packages/web/src/views/system/business/index.vue`
- `frontend/packages/web/src/views/system/log/index.vue`
- `backend/crm/src/main/java/cn/cordys/crm/system`
- `backend/crm/src/main/java/cn/cordys/crm/approval`

**任务：**

- [ ] 梳理用户、部门、角色、权限、菜单模块配置。
- [ ] 建立客户、线索、商机、报价、合同、产品的权限矩阵。
- [ ] 验证角色配置后菜单、按钮、接口权限一致。
- [ ] 梳理审批流程适用资源：报价、合同、发票。
- [ ] 验证审批发起、通过、驳回、撤回、待办、已办。
- [ ] 梳理消息通知和公告是否接入核心业务动作。
- [ ] 梳理系统字典：客户来源、线索状态、商机阶段、失败原因、合同状态、发票状态等。
- [ ] 验证操作日志是否记录关键业务动作。
- [ ] 修复系统支撑模块联调发现的问题。

**验收标准：**

- 不同角色可以看到不同菜单和按钮。
- 无权限接口不能绕过前端直接访问。
- 报价、合同、发票审批可配置、可追踪。
- 关键字典值统一由系统配置或明确常量维护。

### 阶段 8：工作台与数据洞察

**目标：** 在核心业务数据稳定后，补齐工作入口和经营数据展示。

**主要文件：**

- `frontend/packages/web/src/router/routes/modules/workbench.ts`
- `frontend/packages/web/src/views/workbench`
- `frontend/packages/web/src/router/routes/modules/dashboard.ts`
- `frontend/packages/web/src/views/dashboard`
- `backend/crm/src/main/java/cn/cordys/crm/home`
- `backend/crm/src/main/java/cn/cordys/crm/dashboard`

**任务：**

- [ ] 确认工作台需要展示的待办、快捷入口和核心指标。
- [ ] 梳理数据洞察需要展示的图表和指标口径。
- [ ] 明确客户数、线索数、商机金额、报价金额、合同金额、回款金额等计算规则。
- [ ] 验证工作台统计接口和业务明细一致。
- [ ] 验证数据洞察图表权限数据范围。
- [ ] 调整工作台快捷入口，确保角色差异下不出现无权限入口。
- [ ] 修复工作台和数据洞察联调发现的问题。

**验收标准：**

- 工作台指标能追溯到业务明细。
- 数据洞察图表口径有文档记录。
- 数据范围与登录用户角色权限一致。

### 阶段 9：部署准备与交付验收

**目标：** 将开发成果整理为可部署、可回归、可交接的版本。

**主要文件：**

- `frontend/packages/web/package.json`
- `frontend/packages/web/config`
- `backend/pom.xml`
- `backend/app/pom.xml`
- `backend/crm/src/main/resources/migration`
- `installer`
- `docker-compose.stariver.yml` 如存在

**任务：**

- [ ] 执行前端类型检查。
- [ ] 执行前端本地构建。
- [ ] 执行后端 Maven 测试。
- [ ] 执行后端打包。
- [ ] 整理数据库迁移脚本顺序和回滚注意事项。
- [ ] 整理环境变量、端口、数据库、Redis、文件存储等部署配置。
- [ ] 整理默认角色、权限、菜单、字典、审批流初始化数据。
- [ ] 做核心业务链路回归：客户 -> 线索 -> 商机 -> 报价 -> 合同 -> 回款/发票。
- [ ] 输出上线检查清单和已知问题清单。

**验收标准：**

- 前端构建产物可部署。
- 后端 jar 可启动。
- 数据库迁移可在空库或目标库按预期执行。
- 核心销售链路可用。
- 有明确的部署说明、回归用例、已知问题和后续优化计划。

## 6. 里程碑安排

| 里程碑 | 范围 | 建议周期 | 完成标志 |
| --- | --- | --- | --- |
| M0 | 基线与环境确认 | 1-2 天 | 本地前后端可运行，核心页面可进入 |
| M1 | 客户管理 | 3-5 天 | 客户列表、详情、联系人、公海可用 |
| M2 | 线索管理 | 3-5 天 | 线索可转客户、转商机 |
| M3 | 产品与商机 | 5-7 天 | 产品价格可用，商机阶段可推进 |
| M4 | 报价管理 | 4-6 天 | 报价新增、审批、作废、快照可用 |
| M5 | 合同回款发票 | 5-8 天 | 合同、回款、发票主流程可用 |
| M6 | 权限审批系统配置 | 4-7 天 | 角色权限、审批、字典、日志支撑主流程 |
| M7 | 工作台与数据洞察 | 3-5 天 | 关键指标和入口可用 |
| M8 | 部署验收 | 2-4 天 | 构建、打包、迁移、回归通过 |

## 7. 风险与应对

| 风险 | 影响 | 应对 |
| --- | --- | --- |
| 共享 API 包和本项目页面字段不一致 | 页面联调反复失败 | 每个模块先导出接口清单，再动页面 |
| Mock 中间件影响真实接口或登录状态 | 本地结果与部署环境不一致 | 阶段 0 先确认 Mock 开关和真实接口路径 |
| 权限只做前端隐藏，后端未拒绝 | 存在越权风险 | 每个模块都做菜单、按钮、接口、数据范围四类权限验收 |
| 报价和产品价格缺少快照 | 历史报价被产品改价影响 | 报价阶段必须验证快照接口和历史详情 |
| 合同金额、回款、发票口径不一致 | 财务数据不可用 | 合同阶段建立金额一致性检查用例 |
| 数据洞察过早开发 | 指标口径反复变 | 等核心业务模块稳定后再做工作台和看板 |
| 迁移脚本绕过 Flyway | 部署不可重复 | 所有数据库变化进入 migration 目录 |

## 8. 每个模块的交付物

每完成一个模块，需要在项目文档中沉淀以下内容：

- 模块字段清单
- 模块接口清单
- 模块权限矩阵
- 模块状态流转说明
- 模块联调问题清单
- 模块验收记录
- 必要截图或接口样例

建议后续按模块新增文档：

- `docs/modules/customer-development-notes.md`
- `docs/modules/clue-development-notes.md`
- `docs/modules/opportunity-development-notes.md`
- `docs/modules/quotation-development-notes.md`
- `docs/modules/contract-development-notes.md`
- `docs/modules/product-development-notes.md`
- `docs/modules/system-development-notes.md`

## 9. 第一轮执行建议

第一轮不要直接大范围改代码，先执行客户管理模块的开发前置任务。

- [ ] 打开客户管理相关页面，确认页面可访问。
- [ ] 梳理客户字段清单。
- [ ] 梳理客户接口清单。
- [ ] 梳理客户权限矩阵。
- [ ] 验证客户新增、编辑、详情、联系人、公海领取、导出。
- [ ] 形成客户模块问题清单。
- [ ] 按问题清单拆成小任务逐个修复。

客户模块稳定后，再进入线索管理，并重点验证线索转客户和线索转商机。

