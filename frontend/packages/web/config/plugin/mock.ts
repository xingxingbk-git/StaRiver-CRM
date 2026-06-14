/**
 * ====================================================================
 *  🚨 MOCK 插件 — 仅用于前端开发联调，无后端时模拟 API 响应
 *
 *  ⚠️  项目交付/上线前必须删除此文件，并从 vite.config.dev.ts 中移除引用！
 *      删除步骤：
 *        1. 删除 config/plugin/mock.ts
 *        2. 删除 config/vite.config.dev.ts 中的以下两行：
 *             import { mockPlugin } from './plugin/mock';
 *             mockPlugin(),
 *
 *  搜索关键词：MOCK、mock、REMOVE_BEFORE_PRODUCTION
 * ====================================================================
 */

import type { Plugin, Connect } from 'vite';

function ok(data: unknown) {
  return JSON.stringify({ data, success: true, message: 'ok' });
}

function fail(msg = 'error') {
  return JSON.stringify({ data: null, success: false, message: msg });
}

// 读取 POST 请求体
function readBody(req: Connect.IncomingMessage): Promise<any> {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', (chunk: Buffer) => {
      body += chunk.toString();
    });
    req.on('end', () => {
      try {
        resolve(JSON.parse(body));
      } catch {
        resolve({});
      }
    });
  });
}

// MOCK 用户数据 — REMOVE_BEFORE_PRODUCTION
const MOCK_USER = {
  id: 'admin',
  name: '管理员',
  email: 'admin@stariver.com',
  avatar: '',
  sessionId: 'mock-session-id',
  csrfToken: 'mock-csrf-token',
  lastOrganizationId: '',
  organizationIds: [''],
  permissionIds: [
    'DASHBOARD:READ',
    'PRODUCT_MANAGEMENT:READ',
    'PRODUCT_MANAGEMENT:ADD',
    'PRODUCT_MANAGEMENT:UPDATE',
    'PRODUCT_MANAGEMENT:DELETE',
    'CUSTOMER_MANAGEMENT:READ',
    'CLUE_MANAGEMENT:READ',
    'OPPORTUNITY_MANAGEMENT:READ',
    'QUOTE_MANAGEMENT:READ',
    'CONTRACT:READ',
    'SYS_ORGANIZATION:READ',
    'SYSTEM_ROLE:READ',
    'SYSTEM_NOTICE:READ',
    'PROCESS_SETTING:READ',
    'SYSTEM_SETTING:READ',
    'OPERATION_LOG:READ',
  ],
  roles: [{ dataScope: 'ALL' }],
};

// API 请求会经过 Vite proxy（/front/* → /*），所以 mock 需要匹配带 /front 前缀的路径
const PREFIX = '/front';

// MOCK 产品数据 — REMOVE_BEFORE_PRODUCTION
// eslint-disable-next-line prefer-const
let MOCK_PRODUCTS: any[] = [
  {
    id: 'p1',
    name: 'StaRiver AI 中台',
    code: 'STARIVER',
    description: '面向工业场景的 AI 基础设施平台，提供数据接入、特征管理、模型训练与推理编排',
    version: 'v3.8.2',
    status: '开发中',
    productOwner: '陈立文',
    devOwner: '周志远',
    moduleCount: 12,
    requirementCount: 18,
    nextVersion: 'v4.0',
    iconText: 'S',
    iconBg: '#EEF2FF',
    iconColor: '#4F46E5',
    statusBg: 'rgba(22,163,74,0.1)',
    statusColor: '#16A34A',
    price: 0,
    createUser: 'admin',
    updateUser: 'admin',
    createTime: 1716864000000,
    updateTime: 1718073600000,
    createUserName: '管理员',
    updateUserName: '管理员',
    moduleFields: [],
  },
  {
    id: 'p2',
    name: 'OptiQA 智能质检',
    code: 'OPTIQA',
    description: '基于视觉识别的工业质检平台，支持缺陷检测、良率分析与产线实时监控',
    version: 'v2.1.0',
    status: '开发中',
    productOwner: '林岚',
    devOwner: '赵晨',
    moduleCount: 8,
    requirementCount: 10,
    nextVersion: 'v2.2',
    iconText: 'O',
    iconBg: '#FFF7ED',
    iconColor: '#EA580C',
    statusBg: 'rgba(234,88,12,0.1)',
    statusColor: '#EA580C',
    price: 0,
    createUser: 'admin',
    updateUser: 'admin',
    createTime: 1718688000000,
    updateTime: 1719897600000,
    createUserName: '管理员',
    updateUserName: '管理员',
    moduleFields: [],
  },
];

// MOCK 版本路线图数据 — REMOVE_BEFORE_PRODUCTION
const MOCK_ROADMAP = [
  {
    id: 'r1',
    productId: 'p1',
    product: 'StaRiver',
    productType: 'stariver',
    version: 'v3.8.2',
    releaseDate: '2026-05-20',
    status: '已发布',
    statusType: 'released',
    pendingCount: 0,
  },
  {
    id: 'r2',
    productId: 'p1',
    product: 'StaRiver',
    productType: 'stariver',
    version: 'v3.9',
    releaseDate: '2026-08-15',
    status: '开发中',
    statusType: 'developing',
    pendingCount: 6,
  },
  {
    id: 'r3',
    productId: 'p1',
    product: 'StaRiver',
    productType: 'stariver',
    version: 'v4.0',
    releaseDate: '2026-09-30',
    status: '规划中',
    statusType: 'planning',
    pendingCount: 18,
  },
  {
    id: 'r4',
    productId: 'p2',
    product: 'OptiQA',
    productType: 'optiqa',
    version: 'v2.1.0',
    releaseDate: '2026-09-20',
    status: '开发中',
    statusType: 'developing',
    pendingCount: 5,
  },
  {
    id: 'r5',
    productId: 'p2',
    product: 'OptiQA',
    productType: 'optiqa',
    version: 'v2.2',
    releaseDate: '2026-10-30',
    status: '规划中',
    statusType: 'planning',
    pendingCount: 0,
  },
];

// 需要读取 body 的 POST 路由 — REMOVE_BEFORE_PRODUCTION
const postBodyRoutes: Record<
  string,
  (res: Connect.ServerResponse, body: any) => void
> = {
  // 登录
  [`${PREFIX}/login`]: (res, body) => {
    const user = { ...MOCK_USER, id: body?.username || 'admin', name: body?.name || '管理员' };
    res.end(ok(user));
  },
  // 产品列表
  [`${PREFIX}/product/page`]: (res) => {
    res.end(
      ok({
        list: MOCK_PRODUCTS,
        total: MOCK_PRODUCTS.length,
        pageSize: 10,
        current: 1,
      })
    );
  },
  // 新增产品 — 持久化到 MOCK_PRODUCTS
  [`${PREFIX}/product/add`]: (res, body) => {
    const id = `p${Date.now()}`;
    const firstChar = (body?.name || 'P').charAt(0).toUpperCase();
    const newProduct = {
      id,
      name: body?.name || '未命名产品',
      code: body?.code || '',
      description: body?.slogan || '',
      version: body?.version || '',
      status: body?.status || '规划中',
      productOwner: body?.productOwner || '',
      devOwner: body?.devOwner || '',
      moduleCount: 0,
      requirementCount: 0,
      nextVersion: '',
      iconText: firstChar,
      iconBg: '#EEF2FF',
      iconColor: '#4F46E5',
      statusBg: 'rgba(22,163,74,0.1)',
      statusColor: '#16A34A',
      price: 0,
      createUser: 'admin',
      updateUser: 'admin',
      createTime: Date.now(),
      updateTime: Date.now(),
      createUserName: '管理员',
      updateUserName: '管理员',
      moduleFields: [],
    };
    MOCK_PRODUCTS.push(newProduct);
    res.end(ok({ id, success: true }));
  },
  // 更新产品
  [`${PREFIX}/product/update`]: (res, body) => {
    const id = body?.id;
    const index = MOCK_PRODUCTS.findIndex((p) => p.id === id);
    if (index < 0) {
      res.statusCode = 404;
      res.end(fail('产品不存在'));
      return;
    }
    MOCK_PRODUCTS[index] = {
      ...MOCK_PRODUCTS[index],
      ...body,
      description: body?.slogan ?? body?.description ?? MOCK_PRODUCTS[index].description,
      updateTime: Date.now(),
      updateUser: 'admin',
      updateUserName: '管理员',
    };
    res.end(ok({ id, success: true }));
  },
  // 工作台统计：线索
  [`${PREFIX}/home/statistic/lead`]: (res) => {
    res.end(ok({}));
  },
  // 工作台统计：跟进商机
  [`${PREFIX}/home/statistic/opportunity`]: (res) => {
    res.end(ok({}));
  },
  // 工作台统计：赢单商机
  [`${PREFIX}/home/statistic/opportunity/success`]: (res) => {
    res.end(ok({}));
  },
  // 工作台统计：进行中商机
  [`${PREFIX}/home/statistic/opportunity/underway`]: (res) => {
    res.end(ok({}));
  },
};

// GET 路由 — REMOVE_BEFORE_PRODUCTION
const getRoutes: Record<string, (res: Connect.ServerResponse) => void> = {
  // 检查登录状态
  [`${PREFIX}/is-login`]: (res) => {
    res.end(ok(MOCK_USER));
  },
  // 获取 RSA 公钥
  [`${PREFIX}/get-key`]: (res) => {
    res.end(ok(''));
  },
  // License 校验 — 返回永久有效
  [`${PREFIX}/license/validate`]: (res) => {
    res.end(ok({ status: 'valid', license: 'mock', expired: '2099-12-31', count: 999 }));
  },
  // 系统版本
  [`${PREFIX}/system/version`]: (res) => {
    res.end(ok({ currentVersion: '3.0.0', releaseDate: '2025-01-01', latestVersion: '3.0.0', copyright: 'StaRiver' }));
  },
  // 模块导航配置
  [`${PREFIX}/module/nav/config`]: (res) => {
    res.end(ok([]));
  },
  [`${PREFIX}/module/nav/top`]: (res) => {
    res.end(ok([]));
  },
  // 登出
  [`${PREFIX}/logout`]: (res) => {
    res.end(ok(null));
  },
  // 认证方式
  [`${PREFIX}/user/authentication`]: (res) => {
    res.end(ok({ loginType: ['LOCAL'] }));
  },
  // 页面配置
  [`${PREFIX}/ui/display/info`]: (res) => {
    res.end(ok([]));
  },
  // 模块开关配置
  [`${PREFIX}/module/config`]: (res) => {
    res.end(
      ok([
        { moduleKey: 'DASHBOARD', enable: true },
        { moduleKey: 'HOME', enable: true },
        { moduleKey: 'CUSTOMER_MANAGEMENT', enable: true },
        { moduleKey: 'CONTRACT', enable: true },
        { moduleKey: 'ORDER', enable: true },
        { moduleKey: 'CLUE_MANAGEMENT', enable: true },
        { moduleKey: 'BUSINESS_MANAGEMENT', enable: true },
        { moduleKey: 'PRODUCT_MANAGEMENT', enable: true },
        { moduleKey: 'TENDER', enable: true },
        { moduleKey: 'CUSTOM_FORM', enable: true },
        { moduleKey: 'AGENT', enable: true },
      ])
    );
  },
  // 工作台部门树
  [`${PREFIX}/home/statistic/department/tree`]: (res) => {
    res.end(ok([]));
  },
  // 工作台待办统计
  [`${PREFIX}/approval-todo/pending/count`]: (res) => {
    res.end(
      ok({
        total: 0,
        pending: 0,
        approval: 0,
        initiated: 0,
        copied: 0,
      })
    );
  },
  // 工作台首页消息
  [`${PREFIX}/notification/last/list`]: (res) => {
    res.end(ok([]));
  },
  // 工作台未读公告
  [`${PREFIX}/notification/last/announcement/list`]: (res) => {
    res.end(ok([]));
  },
  // SSE 关闭
  [`${PREFIX}/sse/close`]: (res) => {
    res.end(ok(null));
  },
  // 商机表单配置
  [`${PREFIX}/opportunity/module/form`]: (res) => {
    res.end(ok({ fields: [], layouts: [] }));
  },
  // 产品表单配置
  [`${PREFIX}/product/module/form`]: (res) => {
    res.end(ok({ fields: [], layouts: [] }));
  },
  // 产品选项列表
  [`${PREFIX}/product/list/option`]: (res) => {
    res.end(ok(MOCK_PRODUCTS.map((p) => ({ id: p.id, name: p.name }))));
  },
  // 产品版本路线图
  [`${PREFIX}/product/roadmap`]: (res) => {
    res.end(ok(MOCK_ROADMAP));
  },
};

// 支持路径参数的 GET 路由 — REMOVE_BEFORE_PRODUCTION
const getPrefixRoutes: Record<
  string,
  (res: Connect.ServerResponse, id: string) => void
> = {
  // 产品详情
  [`${PREFIX}/product/get/`]: (res, id) => {
    const product = MOCK_PRODUCTS.find((p) => p.id === id);
    if (product) {
      res.end(ok(product));
    } else {
      res.statusCode = 404;
      res.end(fail('产品不存在'));
    }
  },
  // 删除产品
  [`${PREFIX}/product/delete/`]: (res, id) => {
    MOCK_PRODUCTS = MOCK_PRODUCTS.filter((p) => p.id !== id);
    res.end(ok(null));
  },
};

function getFallbackData(path: string) {
  if (path.includes('/page')) {
    return { list: [], total: 0, pageSize: 10, current: 1 };
  }
  if (path.includes('/list') || path.includes('/tree') || path.includes('/option')) {
    return [];
  }
  if (path.includes('/count') || path.includes('/statistic')) {
    return {};
  }
  if (path.includes('/module/form')) {
    return { fields: [], layouts: [] };
  }
  return null;
}

export function mockPlugin(): Plugin {
  return {
    name: 'mock-server',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const method = req.method || 'GET';
        const url = req.url || '';
        const path = url.split('?')[0];

        res.setHeader('Content-Type', 'application/json');

        // POST 请求：精确匹配 + 读取 body
        if (method === 'POST') {
          const postHandler = postBodyRoutes[path];
          if (postHandler) {
            readBody(req).then((body) => {
              postHandler(res, body);
            });
            return;
          }
        }

        // GET 请求：精确匹配
        if (method === 'GET') {
          const getHandler = getRoutes[path];
          if (getHandler) {
            getHandler(res);
            return;
          }

          // GET 请求：前缀匹配（路径参数）
          const prefixKey = Object.keys(getPrefixRoutes).find((pk) => path.startsWith(pk));
          if (prefixKey) {
            const id = path.slice(prefixKey.length);
            if (id) {
              getPrefixRoutes[prefixKey](res, id);
              return;
            }
          }
        }

        // 前端无后端联调兜底：未显式 mock 的 /front 请求返回空数据，避免 Vite proxy 穿透到后端报 500。
        if (path.startsWith(PREFIX)) {
          res.end(ok(getFallbackData(path)));
          return;
        }

        next();
      });
    },
  };
}
