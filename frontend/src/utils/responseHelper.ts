/**
 * 响应状态检查工具
 * 用于兼容后端不一致的状态属性名（isOK vs isOk）
 */

export interface ApiResponse {
  isOK?: boolean
  isOk?: boolean
  msg?: string
  code?: number | string
  [key: string]: any
}

/**
 * 检查API响应是否成功
 * 兼容后端使用的 isOK 和 isOk 两种属性名
 */
export const isResponseSuccess = (response: ApiResponse | null | undefined): boolean => {
  if (!response) return false
  
  // 检查两种可能的属性名
  return response.isOK === true || response.isOk === true
}

/**
 * 获取响应错误消息
 */
export const getResponseMessage = (response: ApiResponse | null | undefined, defaultMsg = '操作失败'): string => {
  if (!response) return defaultMsg
  
  return response.msg || defaultMsg
}

/**
 * 检查响应是否失败，并返回错误消息
 */
export const getResponseError = (response: ApiResponse | null | undefined): string | null => {
  if (isResponseSuccess(response)) return null
  
  return getResponseMessage(response, '操作失败')
}

/**
 * 统一的响应处理器
 */
export const handleResponse = (
  response: ApiResponse | null | undefined,
  successCallback?: (response: ApiResponse) => void,
  errorCallback?: (errorMsg: string) => void
): boolean => {
  if (isResponseSuccess(response)) {
    successCallback?.(response!)
    return true
  } else {
    const errorMsg = getResponseError(response)
    if (errorMsg) {
      errorCallback?.(errorMsg)
    }
    return false
  }
} 