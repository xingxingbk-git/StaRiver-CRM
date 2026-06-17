import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AddProductUrl,
  DeleteProductUrl,
  GetProductListUrl,
  GetProductOptionsUrl,
  GetProductUrl,
  UpdateProductUrl,
} from '@lib/shared/api/requrls/product';
import type { CommonList, TableQueryParams } from '@lib/shared/models/common';
import type { ProductListItem, SaveProductParams, UpdateProductParams } from '@lib/shared/models/product';

export default function useProductApi(CDR: CordysAxios) {
  // 添加产品
  function addProduct(data: SaveProductParams) {
    return CDR.post({ url: AddProductUrl, data });
  }

  // 更新产品
  function updateProduct(data: UpdateProductParams) {
    return CDR.post({ url: UpdateProductUrl, data });
  }

  // 获取产品列表
  function getProductList(data: TableQueryParams) {
    return CDR.post<CommonList<ProductListItem>>({ url: GetProductListUrl, data });
  }

  // 获取产品详情
  function getProduct(id: string) {
    return CDR.get<ProductListItem>({ url: `${GetProductUrl}/${id}` });
  }

  // 删除产品
  function deleteProduct(id: string) {
    return CDR.get({ url: `${DeleteProductUrl}/${id}` });
  }

  // 获取意向产品选项
  function getProductOptions() {
    return CDR.get<{ id: string; name: string }[]>({ url: GetProductOptionsUrl });
  }

  return {
    addProduct,
    updateProduct,
    getProductList,
    getProduct,
    deleteProduct,
    getProductOptions,
  };
}
