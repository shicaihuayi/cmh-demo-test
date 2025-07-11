export interface UploadResponse {
  resCode: string;
  ImageUrl?: string;
  msg?: string;
  isOk: boolean;
}

export interface ApiResponse<T = any> {
  code: number;
  msg: string;
  data?: T;
  isOk: boolean;
} 