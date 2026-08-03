# BiteBolt Admin Panel — Frontend Architecture Plan

> **Phiên bản**: 1.0 | **Cập nhật lần cuối**: 2026-07-19
> **Tác giả**: BiteBolt Frontend Team
> **Phạm vi**: Admin/Staff Web SPA — dành cho vai trò `ADMIN` và `STAFF`
> **Mục tiêu**: Xây dựng một ứng dụng web quản trị chuẩn doanh nghiệp, hiệu năng cao, bảo mật và có khả năng mở rộng

---

## 1. 🏛️ Tổng Quan Kiến Trúc

### 1.1. Phạm Vi Ứng Dụng

Admin Panel là SPA (Single Page Application) phục vụ **duy nhất** 2 vai trò nội bộ:

| Vai trò | Quyền hạn chính |
|---|---|
| `ADMIN` | Quản lý toàn hệ thống, phân quyền, cấu hình, báo cáo doanh thu |
| `STAFF` | Duyệt hồ sơ tài xế, xử lý khiếu nại, quản lý DRIVER |

> ⚠️ **NGHIÊM CẤM**: Admin Panel KHÔNG phục vụ DRIVER và RIDER. Tất cả UI, route và API call phải được thiết kế với nguyên tắc này.

### 1.2. Chiến Lược Authentication

Theo `AUTH_BUSINESS_DESIGN.md`, toàn bộ ADMIN/STAFF **chỉ được phép** đăng nhập qua **Microsoft Entra ID SSO**:

```
Browser → GET /api/v1/auth/sso/entra
       ← redirect tới Microsoft Entra ID
       → Microsoft callback với code + state
       → GET /api/v1/auth/sso/entra/callback (qua API Gateway port 8080)
       ← HttpOnly Cookie (access_token + refresh_token)
       → redirect về /dashboard
```

Sau khi callback thành công, JWT được lưu trong **HttpOnly Cookie** (không accessible bằng JavaScript — bảo mật tối đa). Frontend chỉ cần theo dõi trạng thái đăng nhập qua một lightweight session check endpoint.

---

## 2. 🛠️ Technology Stack

### 2.1. Framework Chính — React + TypeScript (Khuyến nghị)

```
react@18+           — Core UI framework
typescript@5+       — Type safety, IntelliSense, enterprise code quality
vite@5+             — Build tool (nhanh hơn CRA, HMR tức thì)
react-router-dom@6+ — Client-side routing với Data Router
```

> **Lý do chọn React**: Ecosystem rộng nhất, dễ tuyển dụng, phù hợp với bộ thư viện UI doanh nghiệp (Ant Design, shadcn/ui).
>
> **Angular thay thế**: Nếu team quyết định dùng Angular 17+ (Standalone Components), kiến trúc folder, API layer và state management trong tài liệu này vẫn áp dụng được — chỉ đổi React hooks → Angular Services/Signals.

### 2.2. UI Component Library

```
Ant Design (antd@5+)    — Component enterprise-grade: Table, Form, Menu, Modal,...
                          Hỗ trợ sẵn i18n, theme customization, dark mode
@ant-design/icons       — Icon library đồng bộ với Ant Design
```

> Ant Design được chọn vì nó là standard cho B2B/Admin panel ở châu Á, hỗ trợ Tiếng Việt sẵn và có tất cả component cần thiết cho CRUD dashboard.

### 2.3. State Management

```
Zustand@4+              — Global state nhẹ, không boilerplate (auth session, sidebar)
@tanstack/react-query@5 — Server state management: fetch, cache, refetch, mutation
                          Dùng để quản lý tất cả API data
```

> **Nguyên tắc phân tách state**:
> - **Zustand**: UI state (auth user, sidebar collapse, theme, language)
> - **React Query**: Server data (danh sách driver, complaints, reports)

### 2.4. HTTP Client & API Layer

```
axios@1.7+              — HTTP client có interceptor mạnh
axios-retry             — Tự động retry khi network error (3xx/5xx)
```

### 2.5. Form & Validation

```
react-hook-form@7+      — Form state management hiệu năng cao
zod@3+                  — Schema validation, type-safe
@hookform/resolvers     — Bridge giữa zod và react-hook-form
```

### 2.6. Internationalization (i18n)

```
react-i18next@14+       — i18n cho React
i18next@23+             — Core i18n engine
```

> Backend trả về localized message với cả `vi` và `en`. Frontend sẽ đọc ngôn ngữ từ user preference và hiển thị đúng bản dịch.

### 2.7. Utilities

```
dayjs@1.11+             — Date/time formatting (thay thế moment.js)
lodash-es@4+            — Utility functions (debounce, throttle, groupBy)
recharts@2+             — Biểu đồ doanh thu, thống kê (dùng cho dashboard ADMIN)
```

### 2.8. Dev Tools & Quality

```
eslint@9+               — Linting
prettier@3+             — Code formatting
husky@9+                — Git hooks (lint trước khi commit)
lint-staged             — Chỉ lint file được staged
vitest@1+               — Unit testing (tích hợp với Vite)
@testing-library/react  — Component testing
```

---

## 3. 📁 Cấu Trúc Thư Mục (Project Structure)

```
bitebolt-fe/
├── public/
│   ├── favicon.ico
│   └── locales/                    # i18n JSON files
│       ├── vi/
│       │   ├── common.json
│       │   ├── auth.json
│       │   └── dashboard.json
│       └── en/
│           ├── common.json
│           ├── auth.json
│           └── dashboard.json
│
├── src/
│   ├── main.tsx                    # Entry point
│   ├── App.tsx                     # Root component, router setup
│   ├── vite-env.d.ts
│   │
│   ├── api/                        # ★ API Layer (HTTP abstraction)
│   │   ├── axios.config.ts         # Axios instance + interceptors
│   │   ├── endpoints.ts            # All API endpoint constants
│   │   ├── auth.api.ts             # Auth API calls
│   │   ├── user.api.ts             # User API calls
│   │   └── driver.api.ts           # Driver management API
│   │
│   ├── store/                      # ★ Global state (Zustand)
│   │   ├── auth.store.ts           # Current user session, role
│   │   ├── ui.store.ts             # Sidebar, theme, language
│   │   └── index.ts
│   │
│   ├── hooks/                      # Custom React hooks
│   │   ├── useAuth.ts              # Auth state + derived permissions
│   │   ├── useCurrentUser.ts       # Fetch & cache current user info
│   │   ├── usePermission.ts        # Role-based permission check
│   │   └── useTableQuery.ts        # Generic paginated table hook
│   │
│   ├── components/                 # ★ Shared/Common components
│   │   ├── layout/
│   │   │   ├── AppLayout.tsx       # Shell: sidebar + header + content
│   │   │   ├── Sidebar.tsx         # Navigation menu theo role
│   │   │   ├── Header.tsx          # Top bar: user avatar, language switcher
│   │   │   └── PageContainer.tsx   # Wrapper với breadcrumb + title
│   │   ├── common/
│   │   │   ├── LoadingSpinner.tsx
│   │   │   ├── ErrorBoundary.tsx   # Bắt lỗi runtime cho toàn app
│   │   │   ├── EmptyState.tsx
│   │   │   ├── ConfirmModal.tsx    # Dialog xác nhận destructive action
│   │   │   ├── StatusBadge.tsx     # Hiển thị trạng thái: ACTIVE, PENDING,...
│   │   │   └── RoleBadge.tsx
│   │   ├── form/
│   │   │   ├── FormInput.tsx       # Input wrapper với RHF + Zod
│   │   │   ├── FormSelect.tsx
│   │   │   └── FormDatePicker.tsx
│   │   └── table/
│   │       ├── DataTable.tsx       # Generic AntD Table với pagination
│   │       └── TableFilter.tsx     # Thanh tìm kiếm/lọc dùng chung
│   │
│   ├── features/                   # ★ Feature modules (domain-driven)
│   │   ├── auth/
│   │   │   ├── pages/
│   │   │   │   ├── SsoLoginPage.tsx       # Trang login với nút "Đăng nhập với Microsoft"
│   │   │   │   └── SsoCallbackPage.tsx    # Xử lý redirect về sau SSO
│   │   │   ├── components/
│   │   │   │   └── MicrosoftLoginButton.tsx
│   │   │   └── hooks/
│   │   │       └── useSsoLogin.ts
│   │   │
│   │   ├── dashboard/
│   │   │   ├── pages/
│   │   │   │   └── DashboardPage.tsx      # Overview stats (ADMIN only)
│   │   │   └── components/
│   │   │       ├── RevenueChart.tsx
│   │   │       ├── StatCard.tsx
│   │   │       └── RecentActivityTable.tsx
│   │   │
│   │   ├── drivers/                        # STAFF: Quản lý tài xế
│   │   │   ├── pages/
│   │   │   │   ├── DriverListPage.tsx      # Danh sách tài xế + filter
│   │   │   │   ├── DriverDetailPage.tsx    # Xem hồ sơ chi tiết
│   │   │   │   └── DriverVerificationPage.tsx # Duyệt CCCD, bằng lái
│   │   │   ├── components/
│   │   │   │   ├── DriverTable.tsx
│   │   │   │   ├── DriverProfileCard.tsx
│   │   │   │   └── VerificationActions.tsx # Approve/Reject buttons
│   │   │   └── hooks/
│   │   │       ├── useDriverList.ts
│   │   │       └── useDriverActions.ts
│   │   │
│   │   ├── complaints/                     # STAFF: Xử lý khiếu nại
│   │   │   ├── pages/
│   │   │   │   ├── ComplaintListPage.tsx
│   │   │   │   └── ComplaintDetailPage.tsx
│   │   │   └── hooks/
│   │   │       └── useComplaints.ts
│   │   │
│   │   ├── staff/                          # ADMIN: Quản lý STAFF
│   │   │   ├── pages/
│   │   │   │   ├── StaffListPage.tsx
│   │   │   │   └── CreateStaffPage.tsx
│   │   │   └── hooks/
│   │   │       └── useStaffManagement.ts
│   │   │
│   │   └── settings/                       # ADMIN: Cấu hình hệ thống
│   │       ├── pages/
│   │       │   ├── SystemSettingsPage.tsx
│   │       │   └── PricingSettingsPage.tsx
│   │       └── hooks/
│   │           └── useSettings.ts
│   │
│   ├── router/                      # ★ Routing
│   │   ├── index.tsx                # Router definition
│   │   ├── routes.ts                # Route paths constants
│   │   ├── PrivateRoute.tsx         # Guard: yêu cầu đăng nhập
│   │   └── RoleRoute.tsx            # Guard: kiểm tra role (ADMIN/STAFF)
│   │
│   ├── types/                       # TypeScript type definitions
│   │   ├── api.types.ts             # ApiResponse<T>, LocalizedMessage
│   │   ├── auth.types.ts            # User, Role, AuthMethod
│   │   ├── driver.types.ts
│   │   ├── complaint.types.ts
│   │   └── common.types.ts
│   │
│   ├── utils/                       # Pure utility functions
│   │   ├── date.utils.ts            # Format date với dayjs
│   │   ├── string.utils.ts
│   │   ├── permission.utils.ts      # canAccess(role, resource)
│   │   └── error.utils.ts           # Parse ApiResponse errors
│   │
│   └── config/
│       ├── env.ts                   # Typed environment variables
│       ├── i18n.ts                  # i18next configuration
│       └── antd.theme.ts            # Ant Design theme token customization
│
├── .env.development
├── .env.production
├── .eslintrc.cjs
├── .prettierrc
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## 4. 🌐 API Layer — Cấu Hình Axios

### 4.1. File: `src/api/axios.config.ts`

```typescript
import axios, { AxiosError, AxiosInstance } from 'axios';
import axiosRetry from 'axios-retry';
import { ENV } from '@/config/env';
import { useAuthStore } from '@/store/auth.store';
import { useUiStore } from '@/store/ui.store';
import { Routes } from '@/router/routes';

// ─── Tạo Axios Instance ──────────────────────────────────────────────────────
const apiClient: AxiosInstance = axios.create({
  baseURL: ENV.API_BASE_URL,      // http://localhost:8080 (API Gateway)
  timeout: 15_000,
  withCredentials: true,          // ★ BẮT BUỘC: gửi HttpOnly Cookie trong mọi request
  headers: {
    'Content-Type': 'application/json',
    'Accept-Language': 'vi',      // Default: tiếng Việt
  },
});

// ─── Retry Strategy ──────────────────────────────────────────────────────────
axiosRetry(apiClient, {
  retries: 3,
  retryDelay: axiosRetry.exponentialDelay,
  retryCondition: (error) =>
    axiosRetry.isNetworkOrIdempotentRequestError(error) ||
    error.response?.status === 503,
});

// ─── Request Interceptor ─────────────────────────────────────────────────────
apiClient.interceptors.request.use((config) => {
  // Inject ngôn ngữ từ UI store vào mỗi request
  const lang = useUiStore.getState().language;
  config.headers['Accept-Language'] = lang;

  // Inject Trace ID (hỗ trợ distributed tracing)
  const traceId = crypto.randomUUID();
  config.headers['X-Trace-Id'] = traceId;

  return config;
});

// ─── Response Interceptor ────────────────────────────────────────────────────
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const status = error.response?.status;

    // 401 Unauthorized → session hết hạn → redirect về login
    if (status === 401) {
      useAuthStore.getState().clearSession();
      window.location.replace(Routes.LOGIN);
      return Promise.reject(error);
    }

    // 403 Forbidden → không đủ quyền → để UI layer xử lý qua React Query onError

    return Promise.reject(error);
  },
);

export { apiClient };
```

### 4.2. File: `src/api/endpoints.ts`

```typescript
/**
 * Tập trung toàn bộ API endpoint constants.
 * Tránh hard-code URL rải rác trong codebase.
 */
export const API_ENDPOINTS = {
  // ── Authentication ────────────────────────────────────────────────────────
  AUTH: {
    SSO_ENTRA_INITIATE: '/api/v1/auth/sso/entra',
    SSO_ENTRA_CALLBACK: '/api/v1/auth/sso/entra/callback',
    REFRESH: '/api/v1/auth/refresh',
    LOGOUT: '/api/v1/auth/logout',
    ME: '/api/v1/auth/me',          // Lấy thông tin user đang đăng nhập
  },

  // ── User Management ───────────────────────────────────────────────────────
  USERS: {
    LIST: '/api/v1/users',
    DETAIL: (id: string) => `/api/v1/users/${id}`,
    UPDATE: (id: string) => `/api/v1/users/${id}`,
  },

  // ── Driver Management (STAFF) ────────────────────────────────────────────
  DRIVERS: {
    LIST: '/api/v1/drivers',
    DETAIL: (id: string) => `/api/v1/drivers/${id}`,
    VERIFY: (id: string) => `/api/v1/drivers/${id}/verify`,
    SUSPEND: (id: string) => `/api/v1/drivers/${id}/suspend`,
    ACTIVATE: (id: string) => `/api/v1/drivers/${id}/activate`,
  },

  // ── Complaints (STAFF) ───────────────────────────────────────────────────
  COMPLAINTS: {
    LIST: '/api/v1/complaints',
    DETAIL: (id: string) => `/api/v1/complaints/${id}`,
    RESOLVE: (id: string) => `/api/v1/complaints/${id}/resolve`,
  },

  // ── Staff Management (ADMIN only) ────────────────────────────────────────
  STAFF: {
    LIST: '/api/v1/staff',
    CREATE: '/api/v1/staff',
    DETAIL: (id: string) => `/api/v1/staff/${id}`,
    DELETE: (id: string) => `/api/v1/staff/${id}`,
  },

  // ── Reports & Analytics (ADMIN only) ─────────────────────────────────────
  REPORTS: {
    REVENUE: '/api/v1/reports/revenue',
    TRIPS: '/api/v1/reports/trips',
    USERS: '/api/v1/reports/users',
  },
} as const;
```

### 4.3. File: `src/api/auth.api.ts`

```typescript
import { apiClient } from './axios.config';
import { API_ENDPOINTS } from './endpoints';
import type { CurrentUser } from '@/types/auth.types';
import type { ApiResponse } from '@/types/api.types';

export const authApi = {
  /**
   * Lấy URL redirect đến Microsoft Entra ID để bắt đầu SSO.
   * Frontend sẽ redirect browser tới URL này.
   */
  getSsoLoginUrl(): string {
    // Đây là GET redirect — không phải AJAX call.
    // Frontend dùng: window.location.href = authApi.getSsoLoginUrl()
    return `${import.meta.env.VITE_API_BASE_URL}${API_ENDPOINTS.AUTH.SSO_ENTRA_INITIATE}`;
  },

  /**
   * Lấy thông tin user đang đăng nhập từ session (HttpOnly Cookie).
   */
  getMe(): Promise<ApiResponse<CurrentUser>> {
    return apiClient.get(API_ENDPOINTS.AUTH.ME).then((res) => res.data);
  },

  /**
   * Refresh access token (cookie được gửi tự động).
   */
  refreshToken(): Promise<ApiResponse<null>> {
    return apiClient.post(API_ENDPOINTS.AUTH.REFRESH).then((res) => res.data);
  },

  /**
   * Đăng xuất — xóa cookie ở phía server.
   */
  logout(): Promise<ApiResponse<null>> {
    return apiClient.post(API_ENDPOINTS.AUTH.LOGOUT).then((res) => res.data);
  },
};
```

---

## 5. 🔑 TypeScript Type Definitions

### 5.1. File: `src/types/api.types.ts`

Phản ánh đúng cấu trúc `ApiResponse<T>` từ backend:

```typescript
export interface LocalizedMessage {
  code: string;
  vi: string;
  en: string;
}

export interface ApiResponse<T> {
  status: number;
  message: LocalizedMessage;
  data: T | null;
  errors?: LocalizedMessage[];
  traceRequest?: string;
  time: string;          // ISO 8601
}

export interface PaginatedData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export type PaginatedResponse<T> = ApiResponse<PaginatedData<T>>;
```

### 5.2. File: `src/types/auth.types.ts`

```typescript
export type Role = 'ADMIN' | 'STAFF';
export type AuthMethod = 'SSO_ENTRA';

export interface CurrentUser {
  userId: string;          // UUID
  email: string;
  fullName: string;
  avatar?: string;
  role: Role;
  authMethod: AuthMethod;
}
```

---

## 6. 🗂️ State Management

### 6.1. File: `src/store/auth.store.ts`

```typescript
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { CurrentUser } from '@/types/auth.types';

interface AuthState {
  user: CurrentUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: CurrentUser) => void;
  clearSession: () => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      isLoading: true,

      setUser: (user) => set({ user, isAuthenticated: true, isLoading: false }),

      clearSession: () => set({ user: null, isAuthenticated: false, isLoading: false }),

      setLoading: (isLoading) => set({ isLoading }),
    }),
    {
      name: 'BiteBolt-auth',
      storage: createJSONStorage(() => sessionStorage), // sessionStorage: mất khi đóng tab
      partialize: (state) => ({ user: state.user }),    // Chỉ persist user info
    },
  ),
);
```

### 6.2. File: `src/store/ui.store.ts`

```typescript
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

type Language = 'vi' | 'en';
type Theme = 'light' | 'dark';

interface UiState {
  language: Language;
  theme: Theme;
  sidebarCollapsed: boolean;
  setLanguage: (lang: Language) => void;
  setTheme: (theme: Theme) => void;
  toggleSidebar: () => void;
}

export const useUiStore = create<UiState>()(
  persist(
    (set) => ({
      language: 'vi',
      theme: 'light',
      sidebarCollapsed: false,
      setLanguage: (language) => set({ language }),
      setTheme: (theme) => set({ theme }),
      toggleSidebar: () => set((s) => ({ sidebarCollapsed: !s.sidebarCollapsed })),
    }),
    {
      name: 'BiteBolt-ui',
      storage: createJSONStorage(() => localStorage),
    },
  ),
);
```

---

## 7. 🔒 Authentication Flow Implementation

### 7.1. SSO Login Page (`src/features/auth/pages/SsoLoginPage.tsx`)

```typescript
import { Button } from 'antd';
import { authApi } from '@/api/auth.api';

export const SsoLoginPage = () => {
  const handleMicrosoftLogin = () => {
    // Redirect browser toàn bộ đến Microsoft Entra ID qua API Gateway
    window.location.href = authApi.getSsoLoginUrl();
  };

  return (
    <div className="login-container">
      <img src="/BiteBolt-logo.svg" alt="BiteBolt" />
      <h1>BiteBolt Admin Panel</h1>
      <p>Dành cho nhân viên nội bộ BiteBolt</p>
      <Button
        type="primary"
        size="large"
        icon={<MicrosoftIcon />}
        onClick={handleMicrosoftLogin}
      >
        Đăng nhập với Microsoft
      </Button>
    </div>
  );
};
```

### 7.2. SSO Callback Page (`src/features/auth/pages/SsoCallbackPage.tsx`)

```typescript
/**
 * Trang này được load khi Microsoft redirect về:
 * http://localhost:3000/auth/callback
 *
 * Tại thời điểm này, backend đã:
 * 1. Exchange code → JWT
 * 2. Set HttpOnly Cookie
 * 3. Redirect browser về http://localhost:3000/auth/callback
 *
 * Frontend chỉ cần: gọi /api/v1/auth/me để lấy thông tin user,
 * lưu vào store, rồi redirect về /dashboard.
 */
export const SsoCallbackPage = () => {
  const navigate = useNavigate();
  const setUser = useAuthStore((s) => s.setUser);

  useEffect(() => {
    authApi.getMe()
      .then((res) => {
        if (res.data) {
          setUser(res.data);
          navigate(Routes.DASHBOARD, { replace: true });
        }
      })
      .catch(() => {
        navigate(Routes.LOGIN, { replace: true });
      });
  }, []);

  return <LoadingSpinner tip="Đang xác thực..." />;
};
```

### 7.3. Route Guards

```typescript
// src/router/PrivateRoute.tsx
export const PrivateRoute = ({ children }: { children: ReactNode }) => {
  const { isAuthenticated, isLoading } = useAuthStore();

  if (isLoading) return <LoadingSpinner />;
  if (!isAuthenticated) return <Navigate to={Routes.LOGIN} replace />;

  return <>{children}</>;
};

// src/router/RoleRoute.tsx — Kiểm tra role cụ thể
export const RoleRoute = ({
  children,
  allowedRoles,
}: {
  children: ReactNode;
  allowedRoles: Role[];
}) => {
  const user = useAuthStore((s) => s.user);

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to={Routes.FORBIDDEN} replace />;
  }

  return <>{children}</>;
};
```

---

## 8. 🗺️ Routing Structure

### File: `src/router/routes.ts`

```typescript
export const Routes = {
  LOGIN:      '/login',
  CALLBACK:   '/auth/callback',
  FORBIDDEN:  '/403',

  DASHBOARD:  '/dashboard',

  // STAFF + ADMIN
  DRIVERS:    '/drivers',
  DRIVER_DETAIL: (id: string) => `/drivers/${id}`,

  COMPLAINTS: '/complaints',
  COMPLAINT_DETAIL: (id: string) => `/complaints/${id}`,

  // ADMIN only
  STAFF:      '/staff',
  SETTINGS:   '/settings',
  REPORTS:    '/reports',
} as const;
```

### File: `src/router/index.tsx`

```typescript
export const router = createBrowserRouter([
  {
    path: Routes.LOGIN,
    element: <SsoLoginPage />,
  },
  {
    path: Routes.CALLBACK,
    element: <SsoCallbackPage />,
  },
  {
    path: '/',
    element: (
      <PrivateRoute>
        <AppLayout />
      </PrivateRoute>
    ),
    children: [
      { path: Routes.DASHBOARD, element: <DashboardPage /> },

      // ── STAFF + ADMIN routes ─────────────────────────────
      { path: Routes.DRIVERS,   element: <DriverListPage /> },
      { path: '/drivers/:id',   element: <DriverDetailPage /> },
      { path: Routes.COMPLAINTS, element: <ComplaintListPage /> },
      { path: '/complaints/:id', element: <ComplaintDetailPage /> },

      // ── ADMIN only routes ────────────────────────────────
      {
        path: Routes.STAFF,
        element: (
          <RoleRoute allowedRoles={['ADMIN']}>
            <StaffListPage />
          </RoleRoute>
        ),
      },
      {
        path: Routes.SETTINGS,
        element: (
          <RoleRoute allowedRoles={['ADMIN']}>
            <SystemSettingsPage />
          </RoleRoute>
        ),
      },
      {
        path: Routes.REPORTS,
        element: (
          <RoleRoute allowedRoles={['ADMIN']}>
            <ReportsPage />
          </RoleRoute>
        ),
      },
    ],
  },
]);
```

---

## 9. 🎛️ Sidebar Menu — Role-Based Navigation

```typescript
// src/components/layout/Sidebar.tsx
const MENU_ITEMS: MenuConfig[] = [
  {
    key: Routes.DASHBOARD,
    icon: <DashboardOutlined />,
    label: t('nav.dashboard'),
    roles: ['ADMIN', 'STAFF'],
  },
  {
    key: Routes.DRIVERS,
    icon: <CarOutlined />,
    label: t('nav.drivers'),
    roles: ['ADMIN', 'STAFF'],
  },
  {
    key: Routes.COMPLAINTS,
    icon: <MessageOutlined />,
    label: t('nav.complaints'),
    roles: ['ADMIN', 'STAFF'],
  },
  {
    key: Routes.STAFF,
    icon: <TeamOutlined />,
    label: t('nav.staff'),
    roles: ['ADMIN'],         // ← Chỉ ADMIN mới thấy mục này
  },
  {
    key: Routes.REPORTS,
    icon: <BarChartOutlined />,
    label: t('nav.reports'),
    roles: ['ADMIN'],
  },
  {
    key: Routes.SETTINGS,
    icon: <SettingOutlined />,
    label: t('nav.settings'),
    roles: ['ADMIN'],
  },
];

// Filter theo role của user đang đăng nhập
const filteredItems = MENU_ITEMS.filter((item) =>
  item.roles.includes(currentUser.role),
);
```

---

## 10. ⚙️ Environment Configuration

### File: `.env.development`

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=BiteBolt Admin Panel
VITE_APP_VERSION=1.0.0
VITE_DEFAULT_LANGUAGE=vi
```

### File: `.env.production`

```env
VITE_API_BASE_URL=https://api.BiteBolt.com
VITE_APP_NAME=BiteBolt Admin Panel
VITE_APP_VERSION=1.0.0
VITE_DEFAULT_LANGUAGE=vi
```

### File: `src/config/env.ts`

```typescript
/**
 * Typed environment variable access.
 * Throw error nếu biến bắt buộc không được set.
 */
function requireEnv(key: string): string {
  const value = import.meta.env[key];
  if (!value) throw new Error(`Missing required env variable: ${key}`);
  return value;
}

export const ENV = {
  API_BASE_URL: requireEnv('VITE_API_BASE_URL'),
  APP_NAME: import.meta.env.VITE_APP_NAME ?? 'BiteBolt Admin',
  APP_VERSION: import.meta.env.VITE_APP_VERSION ?? '1.0.0',
  DEFAULT_LANGUAGE: import.meta.env.VITE_DEFAULT_LANGUAGE ?? 'vi',
} as const;
```

---

## 11. 🚀 Ant Design Theme Customization

### File: `src/config/antd.theme.ts`

```typescript
import type { ThemeConfig } from 'antd';

export const BiteBoltTheme: ThemeConfig = {
  token: {
    colorPrimary: '#16a34a',         // BiteBolt green
    colorPrimaryHover: '#15803d',
    borderRadius: 8,
    fontFamily: "'Inter', 'SF Pro', sans-serif",
    colorBgContainer: '#ffffff',
    colorTextBase: '#0f172a',
  },
  components: {
    Layout: {
      siderBg: '#0f172a',            // Dark sidebar
      triggerBg: '#1e293b',
    },
    Menu: {
      darkItemBg: '#0f172a',
      darkItemSelectedBg: '#16a34a',
      darkItemHoverBg: '#1e293b',
    },
    Table: {
      headerBg: '#f8fafc',
      rowHoverBg: '#f0fdf4',
    },
  },
};
```

---

## 12. 🌍 Internationalization Setup

### File: `src/config/i18n.ts`

```typescript
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import HttpBackend from 'i18next-http-backend';

i18n
  .use(HttpBackend)
  .use(initReactI18next)
  .init({
    lng: localStorage.getItem('BiteBolt-lang') ?? 'vi',
    fallbackLng: 'vi',
    ns: ['common', 'auth', 'dashboard', 'driver', 'complaint'],
    defaultNS: 'common',
    backend: {
      loadPath: '/locales/{{lng}}/{{ns}}.json',
    },
    interpolation: {
      escapeValue: false,   // React đã escape mặc định
    },
  });

export default i18n;
```

---

## 13. 🛡️ Error Handling Strategy

### File: `src/utils/error.utils.ts`

```typescript
import type { AxiosError } from 'axios';
import type { ApiResponse } from '@/types/api.types';
import { notification } from 'antd';
import i18n from '@/config/i18n';

/**
 * Trích xuất thông báo lỗi từ ApiResponse và hiển thị notification.
 * Dùng trong React Query onError callback.
 */
export function handleApiError(error: unknown): void {
  const lang = i18n.language as 'vi' | 'en';
  const axiosError = error as AxiosError<ApiResponse<null>>;
  const apiResponse = axiosError.response?.data;

  // Lỗi validation: hiển thị danh sách lỗi
  if (apiResponse?.errors?.length) {
    apiResponse.errors.forEach((err) => {
      notification.error({
        message: err[lang],
        duration: 5,
      });
    });
    return;
  }

  // Lỗi đơn
  if (apiResponse?.message) {
    notification.error({
      message: apiResponse.message[lang],
      description: `Trace: ${apiResponse.traceRequest ?? 'N/A'}`,
      duration: 6,
    });
    return;
  }

  // Network error hoặc unknown
  notification.error({
    message: lang === 'vi' ? 'Lỗi kết nối mạng' : 'Network Error',
    description: lang === 'vi' ? 'Vui lòng kiểm tra kết nối và thử lại.' : 'Please check your connection.',
  });
}
```

---

## 14. 📋 Trang & Tính Năng Cần Implement

### ADMIN

| Trang | Mô tả |
|---|---|
| `/dashboard` | KPI tổng quan: doanh thu, số chuyến, users mới |
| `/reports` | Biểu đồ doanh thu theo ngày/tháng, export CSV |
| `/staff` | Danh sách STAFF, tạo mới, vô hiệu hóa |
| `/settings` | Giá cước, cấu hình hệ thống |
| `/drivers` | Xem + quản lý tài xế |
| `/complaints` | Xem + xử lý khiếu nại |

### STAFF

| Trang | Mô tả |
|---|---|
| `/drivers` | Danh sách tài xế, lọc theo trạng thái |
| `/drivers/:id` | Chi tiết hồ sơ: CCCD, bằng lái, biển số |
| `/drivers/:id/verify` | Approve / Reject hồ sơ với ghi chú |
| `/complaints` | Danh sách khiếu nại |
| `/complaints/:id` | Chi tiết + phản hồi khiếu nại |

---

## 15. 🏗️ Khởi Tạo Dự Án

```bash
# 1. Tạo project Vite + React + TypeScript
cd D:\workspace\BiteBolt\bitebolt-fe
npm create vite@latest ./ -- --template react-ts

# 2. Cài đặt dependencies
npm install \
  antd @ant-design/icons \
  react-router-dom \
  zustand \
  @tanstack/react-query \
  axios axios-retry \
  react-hook-form zod @hookform/resolvers \
  react-i18next i18next i18next-http-backend \
  dayjs recharts lodash-es

# 3. Cài đặt dev dependencies
npm install -D \
  @types/lodash-es \
  eslint prettier \
  @typescript-eslint/eslint-plugin \
  vitest @testing-library/react \
  husky lint-staged

# 4. Cấu hình absolute imports trong vite.config.ts
# Thêm resolve.alias: { '@': '/src' }

# 5. Chạy dev server
npm run dev   # → http://localhost:5173
```

---

## 16. 📊 Tóm Tắt Quyết Định Kỹ Thuật

| Quyết định | Lựa chọn | Lý do |
|---|---|---|
| Framework | React 18 + TypeScript | Ecosystem rộng, type safety, dễ onboard team |
| Build Tool | Vite 5 | HMR nhanh, DX tốt, production bundle nhỏ |
| UI Library | Ant Design 5 | B2B enterprise standard, sẵn i18n, đầy đủ component |
| State - Server | TanStack Query | Tự động cache, refetch, loading/error state |
| State - UI | Zustand | Nhẹ, không boilerplate, dễ dùng với TypeScript |
| HTTP Client | Axios + Interceptors | Interceptor mạnh cho auth, retry, trace injection |
| Auth Strategy | Microsoft Entra ID SSO | Theo `AUTH_BUSINESS_DESIGN.md` — chỉ ADMIN/STAFF |
| Token Storage | HttpOnly Cookie | Backend set sẵn, không thể bị XSS đánh cắp |
| Form | RHF + Zod | Hiệu năng cao, type-safe validation |
| i18n | react-i18next | Industry standard, lazy loading namespace |
| CSS | Ant Design Token + CSS Modules | Nhất quán với design system, không class hell |

---

*Tài liệu này là blueprint thiết kế. Mỗi feature module sẽ được implement theo thứ tự ưu tiên từ Auth → Dashboard → Drivers → Complaints → Staff → Settings.*
