import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { RootState } from "@/store";

const STORAGE_KEY = "selectedStudentId";

interface SelectedStudentState {
    selectedStudentId: number | null;
}

const initialState: SelectedStudentState = {
    selectedStudentId: readFromStorage(),
};

function readFromStorage(): number | null {
    if (typeof window === "undefined") return null;
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? Number(raw) : null;
}

const selectedStudentSlice = createSlice({
    name: "selectedStudent",
    initialState,
    reducers: {
        setSelectedStudent(state, action: PayloadAction<number>) {
            state.selectedStudentId = action.payload;
            localStorage.setItem(STORAGE_KEY, String(action.payload));
        },  
        clearSelectedStudent(state) {
            state.selectedStudentId = null;
            localStorage.removeItem(STORAGE_KEY);
        },
    },
});

export const { setSelectedStudent, clearSelectedStudent } = selectedStudentSlice.actions;
export const selectSelectedStudentId = (state: RootState) => state.selectedStudent.selectedStudentId;
export default selectedStudentSlice.reducer;