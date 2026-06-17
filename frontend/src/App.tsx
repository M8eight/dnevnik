import './App.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './views/student/Home'
import Diary from './views/student/Diary'
import GradeTablePage from './views/student/GradeJournalPage'
import TeacherJournal from './views/teacher/TeacherJournal'
import HomeworkJournal from './views/teacher/HomeworkJournal'
import SubjectPage from './views/admin/SubjectPage'
import PeriodPage from './views/admin/PeriodPage'
import UserAdminPage from './views/admin/UserAdminPage'
import SchoolClassPage from './views/admin/SchoolClassPage'
import SchedulePage from './views/admin/SchedulePage'
import Index from './views'
import AcademicYearPage from './views/admin/AcademicYearPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Index />} />
        <Route path="/student/home" element={<Home />} />
        <Route path="/student/diary" element={<Diary />} />
        <Route path="/student/grade" element={<GradeTablePage />} />
        <Route path="/teacher/journal" element={<TeacherJournal />} />
        <Route path="/teacher/homework" element={<HomeworkJournal />} />
        <Route path="/admin/subject" element={<SubjectPage />} />
        <Route path="/admin/period" element={<PeriodPage />} />
        <Route path="/admin/user" element={<UserAdminPage />} />
        <Route path="/admin/school-class" element={<SchoolClassPage />} />
        <Route path="/admin/schedule" element={<SchedulePage />} />
        <Route path="/admin/academic-year" element={<AcademicYearPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
