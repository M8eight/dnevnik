import { createSlice } from "@reduxjs/toolkit"
import type { RootState } from ".."

interface AuthState {
    isAuthenticated: boolean
    username: string | null
    roles: string[]
}

const initialState: AuthState = {
    isAuthenticated: false,
    username: null,
    roles: [],
}

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        setAuth: (state, action) => {
            state.isAuthenticated = true
            state.username = action.payload.username
            state.roles = action.payload.roles
        },
        clearAuth: (state) => {
            Object.assign(state, initialState)
        },
    },
});

export const { setAuth, clearAuth } = authSlice.actions
export const selectRoles = (state: RootState) => state.auth.roles
export const selectHasRole = (role: string) => (state: RootState) => state.auth.roles.includes(role)
export const selectIsAdmin = (state: RootState) => state.auth.roles.includes("ADMIN")
export const selectUsername = (state: RootState) => state.auth.username
export const selectIsAuthenticated = (state: RootState) => state.auth.isAuthenticated
export default authSlice.reducer