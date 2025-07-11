import request from '@/utils/request'

// 会议接口类型定义
export interface Conference {
  conferName: string;
  creater: string;
  conferState: string;
  picture: string;
  content: string;
  stime: string;
  etime: string;
}

export interface SearchParams {
  conferName?: string;
  creater?: string;
  content?: string;
  stime?: string;
}

// 获取会议列表
export const getConferenceList = (params?: any) => {
  return request({
    url: '/confer/list',
    method: 'get',
    params: params
  })
}

// 搜索会议
export const searchConferences = (params: SearchParams) => {
  return request({
    url: '/confer/search',
    method: 'post',
    data: params
  })
}

// 新增会议
export const addConference = (data: Conference) => {
  return request({
    url: '/confer/add',
    method: 'post',
    data
  })
}

// 更新会议
export const updateConference = (data: Conference & { oldName: string }) => {
  return request({
    url: '/confer/update',
    method: 'post',
    data
  })
}

// 删除会议
export const deleteConferences = (conferNames: string[]) => {
  return request({
    url: '/confer/del',
    method: 'post',
    data: conferNames,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 上传会议图片
export const uploadConferenceImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取待审核会议列表
export const getPendingConferenceList = (params?: any) => {
  return request({
    url: '/confer/pendingList',
    method: 'get',
    params: params
  })
}

// 审核通过会议
export const approveConference = (id: number) => {
  return request({
    url: `/confer/approve/${id}`,
    method: 'post'
  })
}

// 审核驳回会议
export const rejectConference = (id: number) => {
  return request({
    url: `/confer/reject/${id}`,
    method: 'post'
  })
} 