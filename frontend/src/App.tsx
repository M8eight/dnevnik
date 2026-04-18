import './App.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './views/home/Home'
import Diary from './views/diary/Diary'
import GradesPage from './views/grade/GradesPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/diary" element={<Diary />} />
        <Route path="/grades" element={<GradesPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
