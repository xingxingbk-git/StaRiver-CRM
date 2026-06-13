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
};

// API 请求会经过 Vite proxy（/front/* → /*），所以 mock 需要匹配带 /front 前缀的路径
const PREFIX = '/front';

// MOCK 路由表 — 所有被拦截的 API 端点，REMOVE_BEFORE_PRODUCTION
const routes: Record<string, (res: Connect.ServerResponse) => void> = {
  // 登录
  [`POST ${PREFIX}/login`]: (res) => {
    res.end(ok(MOCK_USER));
  },
  // 检查登录状态
  [`GET ${PREFIX}/is-login`]: (res) => {
    res.statusCode = 401;
    res.end(fail('未登录'));
  },
  // 获取 RSA 公钥
  [`GET ${PREFIX}/get-key`]: (res) => {
    res.end(ok(''));
  },
  // License 校验 — 返回永久有效
  [`GET ${PREFIX}/license/validate`]: (res) => {
    res.end(ok({ status: 'valid', license: 'mock', expired: '2099-12-31', count: 999 }));
  },
  // 系统版本
  [`GET ${PREFIX}/system/version`]: (res) => {
    res.end(ok({ currentVersion: '3.0.0', releaseDate: '2025-01-01', latestVersion: '3.0.0', copyright: 'StaRiver' }));
  },
  // 模块导航配置
  [`GET ${PREFIX}/module/nav/config`]: (res) => {
    res.end(ok([]));
  },
  [`GET ${PREFIX}/module/nav/top`]: (res) => {
    res.end(ok([]));
  },
  // 登出
  [`GET ${PREFIX}/logout`]: (res) => {
    res.end(ok(null));
  },
  // 认证方式
  [`GET ${PREFIX}/user/authentication`]: (res) => {
    res.end(ok({ loginType: ['LOCAL'] }));
  },
  // 页面配置
  [`GET ${PREFIX}/ui/display/info`]: (res) => {
    res.end(ok([]));
  },
  // 模块开关配置
  [`GET ${PREFIX}/module/config`]: (res) => {
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
};

export function mockPlugin(): Plugin {
  return {
    name: 'mock-server',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const method = req.method || 'GET';
        const url = req.url || '';
        const key = `${method} ${url.split('?')[0]}`;
        const handler = routes[key];
        if (handler) {
          res.setHeader('Content-Type', 'application/json');
          handler(res);
        } else {
          next();
        }
      });
    },
  };
}
