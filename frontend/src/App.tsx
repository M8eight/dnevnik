import './App.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './views/home/Home'
import Diary from './views/diary/Diary'
import GradesPage from './views/grade/GradesPage'
import TeacherJournal from './views/teacher/TeacherJournal'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/diary" element={<Diary />} />
        <Route path="/grades" element={<GradesPage />} />
        <Route path="/teacher/journal" element={<TeacherJournal />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
