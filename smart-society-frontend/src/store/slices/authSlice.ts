import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit'
import { authApi, type LoginRequest } from '@/api/auth.api'
import { storage } from '@/lib/utils'
import type { AuthState, User } from '@/types'

const initialState: AuthState = {
  user:            storage.get('user'),
  accessToken:     storage.get('accessToken'),
  refreshToken:    storage.get('refreshToken'),
  isAuthenticated: !!storage.get('accessToken'),
  isLoading:       false,
}

// ─── Async thunks ─────────────────────────────────────────────────────────────

export const loginThunk = createAsyncThunk(
  'auth/login',
  async (credentials: LoginRequest, { rejectWithValue }) => {
    try {
      const { data } = await authApi.login(credentials)
      return data.data
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      return rejectWithValue(err.response?.data?.message ?? 'Login failed')
    }
  },
)

export const logoutThunk = createAsyncThunk(
  'auth/logout',
  async (_, { getState }) => {
    const state = getState() as { auth: AuthState }
    const refreshToken = state.auth.refreshToken
    if (refreshToken) {
      try { await authApi.logout(refreshToken) } catch { /* ignore — clear client anyway */ }
    }
    storage.clear()
  },
)

export const getProfileThunk = createAsyncThunk(
  'auth/getProfile',
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await authApi.getProfile()
      return data.data
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      return rejectWithValue(err.response?.data?.message ?? 'Failed to get profile')
    }
  },
)

// ─── Slice ────────────────────────────────────────────────────────────────────

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action: PayloadAction<{
      user: User; accessToken: string; refreshToken: string
    }>) => {
      state.user            = action.payload.user
      state.accessToken     = action.payload.accessToken
      state.refreshToken    = action.payload.refreshToken
      state.isAuthenticated = true
      storage.set('user',          action.payload.user)
      storage.set('accessToken',   action.payload.accessToken)
      storage.set('refreshToken',  action.payload.refreshToken)
    },
    clearCredentials: (state) => {
      state.user            = null
      state.accessToken     = null
      state.refreshToken    = null
      state.isAuthenticated = false
      storage.clear()
    },
    updateUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload
      storage.set('user', action.payload)
    },
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(loginThunk.pending, (state) => { state.isLoading = true })
      .addCase(loginThunk.fulfilled, (state, action) => {
        state.isLoading       = false
        state.user            = action.payload.user
        state.accessToken     = action.payload.accessToken
        state.refreshToken    = action.payload.refreshToken
        state.isAuthenticated = true
        storage.set('user',         action.payload.user)
        storage.set('accessToken',  action.payload.accessToken)
        storage.set('refreshToken', action.payload.refreshToken)
      })
      .addCase(loginThunk.rejected, (state) => { state.isLoading = false })
      // Logout
      .addCase(logoutThunk.fulfilled, (state) => {
        state.user            = null
        state.accessToken     = null
        state.refreshToken    = null
        state.isAuthenticated = false
      })
      // Profile
      .addCase(getProfileThunk.fulfilled, (state, action) => {
        state.user = action.payload
        storage.set('user', action.payload)
      })
  },
})

export const { setCredentials, clearCredentials, updateUser } = authSlice.actions
export default authSlice.reducer

// ─── Selectors ────────────────────────────────────────────────────────────────

export const selectCurrentUser        = (state: { auth: AuthState }) => state.auth.user
export const selectIsAuthenticated    = (state: { auth: AuthState }) => state.auth.isAuthenticated
export const selectIsLoading          = (state: { auth: AuthState }) => state.auth.isLoading
export const selectUserRole           = (state: { auth: AuthState }) => state.auth.user?.role
export const selectUserSocietyId      = (state: { auth: AuthState }) => state.auth.user?.societyId