import './App.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './views/student/Home'
import Diary from './views/student/Diary'
import GradesPage from './views/student/GradesPage'
import TeacherJournal from './views/teacher/TeacherJournal'
import HomeworkJournal from './views/teacher/HomeworkJournal'
import SubjectPage from './views/admin/SubjectPage'
import PeriodPage from './views/admin/PeriodPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/student/home" element={<Home />} />
        <Route path="/student/diary" element={<Diary />} />
        <Route path="/student/grades" element={<GradesPage />} />
        <Route path="/teacher/journal" element={<TeacherJournal />} />
        <Route path="/teacher/homework" element={<HomeworkJournal />} />
        <Route path="/admin/subject" element={<SubjectPage />} />
        <Route path="/admin/period" element={<PeriodPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
