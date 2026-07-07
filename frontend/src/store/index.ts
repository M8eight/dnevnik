import { configureStore } from "@reduxjs/toolkit";
import scheduleReducer from "./slices/scheduleSlice";
import authSlice from "./slices/authSlice";


export const store = configureStore({
    reducer: {
        schedule: scheduleReducer,
        auth: authSlice
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;