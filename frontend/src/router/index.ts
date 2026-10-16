import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/HomeView.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'products',
          name: 'ProductList',
          component: () => import('@/views/ProductListView.vue'),
          meta: { title: '全部商品' },
        },
        {
          path: 'products/:id',
          name: 'ProductDetail',
          component: () => import('@/views/ProductDetailView.vue'),
          meta: { title: '商品详情' },
        },
        {
          path: 'near-expiry',
          name: 'NearExpiry',
          component: () => import('@/views/NearExpiryView.vue'),
          meta: { title: '临期专区' },
        },
        {
          path: 'ranking',
          name: 'Ranking',
          component: () => import('@/views/RankingView.vue'),
          meta: { title: '好用排行' },
        },
        {
          path: 'seckill/checkout',
          name: 'SeckillCheckout',
          component: () => import('@/views/SeckillCheckoutView.vue'),
          meta: { title: '秒杀结算', requiresAuth: true },
        },
        {
          path: 'seckill/:activityId?',
          name: 'Seckill',
          component: () => import('@/views/SeckillView.vue'),
          meta: { title: '秒杀专区' },
        },
        {
          path: 'community',
          name: 'Community',
          component: () => import('@/views/CommunityView.vue'),
          meta: { title: '宠物社区' },
        },
        {
          path: 'pets',
          name: 'PetArchive',
          component: () => import('@/views/PetArchiveView.vue'),
          meta: { title: '宠物档案' },
        },
        {
          path: 'pets/:id',
          name: 'PetBreedDetail',
          component: () => import('@/views/PetBreedDetailView.vue'),
          meta: { title: '品种详情' },
        },
        {
          path: 'community/:id',
          name: 'CommunityDetail',
          component: () => import('@/views/CommunityDetailView.vue'),
          meta: { title: '动态详情' },
        },
        {
          path: 'assistant',
          name: 'Assistant',
          component: () => import('@/views/AssistantView.vue'),
          meta: { title: '问答助手', requiresAuth: true },
        },
        {
          path: 'notices',
          name: 'Notices',
          component: () => import('@/views/NoticesView.vue'),
          meta: { title: '消息中心', requiresAuth: true },
        },
        {
          path: 'cart',
          name: 'Cart',
          component: () => import('@/views/CartView.vue'),
          meta: { title: '购物车', requiresAuth: true },
        },
        {
          path: 'checkout',
          name: 'Checkout',
          component: () => import('@/views/CheckoutView.vue'),
          meta: { title: '结算', requiresAuth: true },
        },
        {
          path: 'orders',
          name: 'OrderList',
          component: () => import('@/views/OrderListView.vue'),
          meta: { title: '我的订单', requiresAuth: true },
        },
        {
          path: 'orders/:id',
          name: 'OrderDetail',
          component: () => import('@/views/OrderDetailView.vue'),
          meta: { title: '订单详情', requiresAuth: true },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/ProfileCenterView.vue'),
          meta: { title: '个人中心', requiresAuth: true },
        },
        {
          path: 'pay/:orderId',
          name: 'Pay',
          component: () => import('@/views/PayView.vue'),
          meta: { title: '收银台', requiresAuth: true },
        },
        {
          path: 'login',
          name: 'Login',
          component: () => import('@/views/LoginView.vue'),
          meta: { title: '登录' },
        },
        {
          path: 'register',
          name: 'Register',
          component: () => import('@/views/RegisterView.vue'),
          meta: { title: '注册' },
        },
        {
          path: 'forgot',
          name: 'ForgotPassword',
          component: () => import('@/views/ForgotPasswordView.vue'),
          meta: { title: '找回密码' },
        },
      ],
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/AdminLoginView.vue'),
      meta: { title: '后台登录' },
    },
    {
      path: '/admin',
      component: () => import('@/layout/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/users' },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/AdminDashboardView.vue'),
          meta: { title: '数据看板' },
        },
        {
          path: 'products',
          name: 'AdminProducts',
          component: () => import('@/views/admin/AdminProductView.vue'),
          meta: { title: '商品管理' },
        },
        {
          path: 'orders',
          name: 'AdminOrders',
          component: () => import('@/views/admin/AdminOrderView.vue'),
          meta: { title: '订单管理' },
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/AdminUserView.vue'),
          meta: { title: '用户管理' },
        },
        {
          path: 'seckill',
          name: 'AdminSeckill',
          component: () => import('@/views/admin/AdminSeckillView.vue'),
          meta: { title: '秒杀管理' },
        },
        {
          path: 'after-sale',
          name: 'AdminAfterSale',
          component: () => import('@/views/admin/AdminAfterSaleView.vue'),
          meta: { title: '售后管理' },
        },
        {
          path: 'pet-breed',
          name: 'AdminPetBreed',
          component: () => import('@/views/admin/AdminPetBreedView.vue'),
          meta: { title: '宠物档案' },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  const adminStore = useAdminStore()
  if (to.meta.requiresAdmin && !adminStore.isLogin) {
    return { name: 'AdminLogin' }
  }
  if (to.meta.requiresAuth && !userStore.token) {
    return { name: 'Login' }
  }
  return true
})

export default router