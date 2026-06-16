import CDR from '@/api/http';

export interface ProductModulePayload {
  name: string;
  ownerId?: string;
  ownerName?: string;
  pendingCount?: number;
  children?: ProductModulePayload[];
}

export interface ProductSavePayload {
  id?: string;
  code: string;
  name: string;
  version?: string;
  status?: string;
  releaseDate?: string;
  slogan?: string;
  productOwner?: string;
  productOwnerId?: string;
  devOwner?: string;
  devOwnerId?: string;
  modules?: ProductModulePayload[];
}

export interface RequirementSavePayload {
  title: string;
  type: string;
  source: string;
  product?: string;
  productId?: string;
  release?: string;
  priority: string;
  description: string;
  acceptance: string;
}

export function getProductList() {
  return CDR.post<{ list: any[]; total: number }>({
    url: '/product-management/products/page',
    data: { current: 1, pageSize: 100 },
  });
}

export function getProductDetail(id: string) {
  return CDR.get<any>({
    url: `/product-management/products/${id}`,
  });
}

export function addProduct(data: ProductSavePayload) {
  return CDR.post<any>({
    url: '/product-management/products',
    data,
  });
}

export function updateProduct(data: ProductSavePayload) {
  return CDR.post<any>({
    url: '/product-management/products/update',
    data,
  });
}

export function getRoadmap() {
  return CDR.get<any[]>({
    url: '/product-management/roadmap',
  });
}

export async function getUserOptions(keyword = '') {
  const options = await CDR.get<Array<{ id?: string; name?: string; label?: string; value?: string }>>({
    url: '/user/option',
    params: keyword ? { keyword } : {},
  });
  const normalized = (options || []).map((option) => ({
    label: option.label || option.name || '',
    value: option.value || option.id || '',
  }));
  if (!keyword) {
    return normalized;
  }
  return normalized.filter((option) => option.label.includes(keyword));
}

export function getRequirementList() {
  return CDR.post<{ list: any[]; total: number }>({
    url: '/product-management/requirements/page',
    data: { current: 1, pageSize: 200 },
  });
}

export function getRequirementDetail(id: string) {
  return CDR.get<any>({
    url: `/product-management/requirements/${id}`,
  });
}

export function addRequirement(data: RequirementSavePayload) {
  return CDR.post<any>({
    url: '/product-management/requirements',
    data,
  });
}
