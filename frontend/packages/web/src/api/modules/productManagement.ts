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

export interface ProductVersionSavePayload {
  id?: string;
  productId: string;
  version: string;
  status?: string;
  releaseDate?: string;
  description?: string;
  productOwnerId?: string;
  productOwner?: string;
  devOwnerId?: string;
  devOwner?: string;
  attachmentIds?: string[];
}

export interface RequirementSavePayload {
  id?: string;
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

export interface RequirementAdvanceStagePayload {
  content?: string;
  attachmentIds?: string[];
  moduleId?: string;
  versionId?: string;
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

export function addProductVersion(data: ProductVersionSavePayload) {
  return CDR.post<any>(
    {
      url: '/product-management/versions',
      data,
    },
    {
      ignoreCancelToken: true,
    }
  );
}

export function updateProductVersionStatus(id: string, status: string) {
  return CDR.post<any>(
    {
      url: `/product-management/versions/${id}/status`,
      data: { status },
    },
    {
      ignoreCancelToken: true,
    }
  );
}

export function deleteProductVersion(id: string) {
  return CDR.post<void>(
    {
      url: `/product-management/versions/${id}/delete`,
    },
    {
      ignoreCancelToken: true,
    }
  );
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

export function updateRequirement(data: RequirementSavePayload) {
  return CDR.post<any>({
    url: '/product-management/requirements/update',
    data,
  });
}

export function deleteRequirement(id: string) {
  return CDR.post<void>({
    url: `/product-management/requirements/${id}/delete`,
  });
}

export function submitRequirementForReview(id: string) {
  return CDR.post<any>({
    url: `/product-management/requirements/${id}/submit-review`,
  });
}

export function revokeRequirementReview(id: string) {
  return CDR.post<any>({
    url: `/product-management/requirements/${id}/revoke-review`,
  });
}

export function advanceRequirementStage(id: string, data?: RequirementAdvanceStagePayload) {
  return CDR.post<any>({
    url: `/product-management/requirements/${id}/advance-stage`,
    data,
  });
}

export function returnRequirementStage(id: string, data?: RequirementAdvanceStagePayload) {
  return CDR.post<any>({
    url: `/product-management/requirements/${id}/return-stage`,
    data,
  });
}
