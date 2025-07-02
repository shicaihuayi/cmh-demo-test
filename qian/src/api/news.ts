import request from '@/utils/request'

// 获取news list
export function getNewsList(data: any) {
  return request({
    url: '/article/pageList',
    method: 'post',
    headers:{
      isToken:true,
    },
    data
  })
}

// 上传图片
export function uploadImg(data: any) {
  return request({
    url: '/file/upload',
    method: 'post',
    data
  })
}
// 更新news
export function updateNews(data: any) {
  return request({
    url: '/article/update',
    method: 'post',
    data
  })
}
// 创建 news
export function addNews(data: any) {
  return request({
    url: '/article/add',
    method: 'post',
    data
  })
}
// 删除 news
export function deleteNews(id: any) {
  return request({
    url: '/article/delete',
    method: 'post',
    data:{
      id
    }
  })
}

// 根据id获取news详情
export function getNewsDetail(id: any) {
  return request({
    url: '/article/detail',
    method: 'get',
    params:{
      id
    }
  })
}

