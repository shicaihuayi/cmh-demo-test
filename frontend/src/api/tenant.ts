import request from '@/utils/request'

// 租户接口类型定义
export interface TenantInfo {
  id?: number
  name: string
  linkman: string
  tel: string
  desc: string
  admin: string
  sign: string
  state: number
  mail: string
  coverUrl: string
}

export interface SearchParams {
  name?: string
  linkman?: string
  tel?: string
  sign?: string
}

// 获取租户列表
export const getTenantList = () => {
  return request({
    url: '/company/list',
    method: 'get'
  })
}

// 搜索租户
export const searchTenants = (params: SearchParams) => {
  return request({
    url: '/company/search',
    method: 'post',
    data: params
  })
}

// 新增租户
export const addTenant = (data: TenantInfo) => {
  const formData = new FormData()
  Object.entries(data).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      formData.append(key, value.toString())
    }
  })
  
  return request({
    url: '/company/add',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 修改租户
export const updateTenant = (data: TenantInfo, oldname?: string) => {
  console.log('=== updateTenant API调试 ===')
  console.log('传入的数据:', data)
  console.log('原始公司名称:', oldname)
  
  const formData = new FormData()
  Object.entries(data).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      if (key === 'id') {
        formData.append(key, value.toString())
      } else {
        formData.append(key, value.toString())
      }
      console.log(`FormData添加: ${key} = ${value}`)
    }
  })
  
  // 添加oldname参数，如果没有提供则使用当前名称
  if (oldname) {
    formData.append('oldname', oldname)
    console.log(`FormData添加: oldname = ${oldname}`)
  } else if (data.name) {
    formData.append('oldname', data.name)
    console.log(`FormData添加: oldname = ${data.name} (使用当前名称)`)
  }
  
  console.log('FormData内容:')
  for (let [key, value] of formData.entries()) {
    console.log(`  ${key}: ${value}`)
  }
  
  console.log('准备发送请求到: /company/update')
  
  return request({
    url: '/company/update',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then((response: any) => {
    console.log('updateTenant API响应:', response)
    return response
  }).catch((error: any) => {
    console.error('updateTenant API错误:', error)
    throw error
  })
}

// 删除租户
export const deleteTenant = (ids: number[]) => {
  return request({
    url: '/company/del',
    method: 'post',
    data: ids,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 上传图片
export const uploadImage = (file: File) => {
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

// 创建管理员用户
export const createAdminUser = (data: {
  name: string
  companyName: string
  role: string
  pwd: string
}) => {
  const formData = new FormData()
  Object.entries(data).forEach(([key, value]) => {
    formData.append(key, value)
  })
  
  return request({
    url: '/user/add',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新部门名称
export const updateDepartmentName = (oldName: string, newName: string) => {
  const formData = new FormData()
  formData.append('oldName', oldName)
  formData.append('newName', newName)
  
  return request({
    url: '/department/updateName',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除部门
export const deleteDepartment = (companyName: string) => {
  const formData = new FormData()
  formData.append('companyName', companyName)
  
  return request({
    url: '/department/del',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新用户名称
export const updateUserName = (oldName: string, newName: string) => {
  const formData = new FormData()
  formData.append('oldName', oldName)
  formData.append('newName', newName)
  
  return request({
    url: '/user/updateName',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新管理员
export const updateAdmin = (oldAdmin: string, newAdmin: string) => {
  const formData = new FormData()
  formData.append('oldadmin', oldAdmin)
  formData.append('name', newAdmin)
  
  return request({
    url: '/user/updateAdmin',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除用户
export const deleteUser = (companyName: string) => {
  const formData = new FormData()
  formData.append('companyName', companyName)
  
  return request({
    url: '/user/delUser',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 