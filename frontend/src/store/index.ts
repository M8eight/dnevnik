import { configureStore } from "@reduxjs/toolkit";
import scheduleReducer from "./slices/scheduleSlice";
import authSlice from "./slices/authSlice";
import selectedStudentSlice from "./slices/selectedStudentSlice";


export const store = configureStore({
    reducer: {
        schedule: scheduleReducer,
        auth: authSlice,
        selectedStudent: selectedStudentSlice,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;