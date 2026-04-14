import { startOfWeek, addDays, subDays } from 'date-fns';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface ScheduleState {
  currentWeekStart: string; 
}

const initialState: ScheduleState = {
  currentWeekStart: startOfWeek(new Date(), { weekStartsOn: 1 }).toISOString(),
};

export const scheduleSlice = createSlice({
  name: 'schedule',
  initialState,
  reducers: {
    nextWeek: (state) => {
      const currentDate = new Date(state.currentWeekStart);
      state.currentWeekStart = addDays(currentDate, 7).toISOString();
    },
    prevWeek: (state) => {
      const currentDate = new Date(state.currentWeekStart);
      state.currentWeekStart = subDays(currentDate, 7).toISOString();
    },
    setWeek: (state, action: PayloadAction<string>) => {
      state.currentWeekStart = action.payload;
    },
  },
});

export const { nextWeek, prevWeek, setWeek } = scheduleSlice.actions;
export default scheduleSlice.reducer;