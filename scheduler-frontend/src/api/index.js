import axios from 'axios'

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 回應攔截：統一處理 ApiResponse 包裝與錯誤
http.interceptors.response.use(
  (response) => {
    const body = response.data
    // 後端統一回傳 { code, message, data }
    if (body && typeof body.code === 'number') {
      if (body.code >= 200 && body.code < 300) {
        return body.data
      }
      // 後端業務錯誤（如帳號重複、找不到資源）
      return Promise.reject(new Error(body.message || '操作失敗'))
    }
    // 非標準格式直接回傳（相容舊端點）
    return response.data
  },
  (error) => {
    const msg =
      error.response?.data?.message ||
      error.message ||
      '網路錯誤，請稍後再試'
    return Promise.reject(new Error(msg))
  }
)

export default http
