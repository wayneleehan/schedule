import { createRouter, createWebHistory } from 'vue-router'
import Login from '../view/Login.vue'
import Schedule from '../view/Schedule.vue'
import Result from '../view/Result.vue'
import Grade from '../view/setup/Grade.vue'
import Type from '../view/setup/Type.vue'
import Courses from '../view/setup/Courses.vue'
import Availability from '../view/setup/Availability.vue'
import TeacherList from '../view/admin/TeacherList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    
    // Setup Flow
    { path: '/setup/grade', component: Grade },
    { path: '/setup/type', component: Type },
    { path: '/setup/courses', component: Courses },
    { path: '/setup/availability', component: Availability },
    
    // Main App
    { path: '/schedule', component: Schedule },
    { path: '/result', component: Result },
    
    // Admin
    { path: '/admin/teachers', component: TeacherList }
  ]
})

export default router