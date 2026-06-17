import type { ModuleField } from './common';

export interface ProductListItem {
  id: string;
  code: string;
  name: string;
  version: string;
  nextVersion: string;
  status: string;
  releaseDate: string;
  slogan: string;
  productOwner: string;
  productOwnerId: string;
  devOwner: string;
  devOwnerId: string;
  createUser: string;
  updateUser: string;
  createTime: number;
  updateTime: number;
  createUserName: string;
  updateUserName: string;
}

export interface SaveProductParams {
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
}

export interface UpdateProductParams extends SaveProductParams {
  id: string;
}
