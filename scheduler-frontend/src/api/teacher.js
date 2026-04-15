import http from './index'

// ── 教師基礎 CRUD ──────────────────────────────────────────
export const teacherApi = {
  /** 取得所有教師 */
  getAll: () => http.get('/teachers'),

  /** 註冊 */
  register: (payload) => http.post('/teachers/register', payload),

  /** 登入 */
  login: (payload) => http.post('/teachers/login', payload),

  /** 更新年級 */
  updateGrade: (id, grade) => http.post(`/teachers/${id}/grade`, grade),

  /** 更新教師類型（HOMEROOM / SUBJECT） */
  updateType: (id, type) => http.post(`/teachers/${id}/type`, type),

  // ── 排課 ──────────────────────────────────────────────────
  /** 觸發自動排課 */
  autoSchedule: (id) => http.post(`/teachers/${id}/auto-schedule`),

  /** 取得個人課表 */
  getMySchedule: (id) => http.get(`/teachers/${id}/schedule`),

  /** 取得年級課表 */
  getGradeSchedule: (grade) => http.get(`/teachers/grade/${grade}/schedule`),

  /** 手動儲存課表 */
  saveSchedule: (id, items) => http.post(`/teachers/${id}/schedule`, items),
}

// ── 課程需求 CRUD ──────────────────────────────────────────
export const courseApi = {
  /** 取得課程需求 */
  getCourses: (teacherId) => http.get(`/teachers/${teacherId}/courses`),

  /** 覆蓋式更新（整批替換） */
  updateCourses: (teacherId, courses) =>
    http.put(`/teachers/${teacherId}/courses`, courses),

  /** 新增單筆 */
  addCourse: (teacherId, course) =>
    http.post(`/teachers/${teacherId}/courses`, course),

  /** 更新單筆 */
  updateCourse: (teacherId, courseId, course) =>
    http.put(`/teachers/${teacherId}/courses/${courseId}`, course),

  /** 刪除單筆 */
  deleteCourse: (teacherId, courseId) =>
    http.delete(`/teachers/${teacherId}/courses/${courseId}`),
}

// ── 可用時段 CRUD ──────────────────────────────────────────
export const availabilityApi = {
  /** 取得忙碌時段 */
  getAvailability: (teacherId) =>
    http.get(`/teachers/${teacherId}/availability`),

  /** 覆蓋式更新（整批替換） */
  updateAvailability: (teacherId, slots) =>
    http.put(`/teachers/${teacherId}/availability`, slots),

  /** 新增單筆 */
  addSlot: (teacherId, slot) =>
    http.post(`/teachers/${teacherId}/availability`, slot),

  /** 刪除單筆 */
  deleteSlot: (teacherId, slotId) =>
    http.delete(`/teachers/${teacherId}/availability/${slotId}`),

  /** 清空全部 */
  clearAll: (teacherId) =>
    http.delete(`/teachers/${teacherId}/availability`),
}
