/**
 * ====================================================================
 *  MOCK 产品 API — 仅用于前端开发联调，无后端时模拟 API 调用
 *
 *  ⚠️  项目交付/上线前必须删除此文件！
 *      搜索关键词：MOCK、REMOVE_BEFORE_PRODUCTION
 * ====================================================================
 */

// 通用请求封装，走 /front 前缀代理
async function mockGet<T = any>(url: string): Promise<T> {
  const res = await fetch(`/front${url}`);
  const json = await res.json();
  return json.data;
}

async function mockPost<T = any>(url: string, data?: any): Promise<T> {
  const res = await fetch(`/front${url}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data || {}),
  });
  const json = await res.json();
  return json.data;
}

// MOCK — REMOVE_BEFORE_PRODUCTION
export async function getProductList() {
  return mockPost('/product/page', { current: 1, pageSize: 20 });
}

// MOCK — REMOVE_BEFORE_PRODUCTION
export async function getProductDetail(id: string) {
  return mockGet(`/product/get/${id}`);
}

// MOCK — REMOVE_BEFORE_PRODUCTION
export async function addProduct(data: Record<string, any>) {
  return mockPost('/product/add', data);
}

// MOCK — REMOVE_BEFORE_PRODUCTION
export async function updateProduct(data: Record<string, any>) {
  return mockPost('/product/update', data);
}

// MOCK — REMOVE_BEFORE_PRODUCTION
export async function getRoadmap() {
  return mockGet('/product/roadmap');
}
