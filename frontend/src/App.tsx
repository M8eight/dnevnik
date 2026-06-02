import './App.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './views/student/Home'
import Diary from './views/student/Diary'
import GradesPage from './views/student/GradeTablePage'
import TeacherJournal from './views/teacher/TeacherJournal'
import HomeworkJournal from './views/teacher/HomeworkJournal'
import SubjectPage from './views/admin/SubjectPage'
import PeriodPage from './views/admin/PeriodPage'
import UserAdminPage from './views/admin/UserAdminPage'
import SchoolClassPage from './views/admin/SchoolClassPage'
import SchedulePage from './views/admin/SchedulePage'
import Index from './views'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Index />} />
        <Route path="/student/home" element={<Home />} />
        <Route path="/student/diary" element={<Diary />} />
        <Route path="/student/grade" element={<GradesPage />} />
        <Route path="/teacher/journal" element={<TeacherJournal />} />
        <Route path="/teacher/homework" element={<HomeworkJournal />} />
        <Route path="/admin/subject" element={<SubjectPage />} />
        <Route path="/admin/period" element={<PeriodPage />} />
        <Route path="/admin/user" element={<UserAdminPage />} />
        <Route path="/admin/school-class" element={<SchoolClassPage />} />
        <Route path="/admin/schedule" element={<SchedulePage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
