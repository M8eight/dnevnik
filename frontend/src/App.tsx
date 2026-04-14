import './App.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './views/home/Home'
import Diary from './views/diary/Diary'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/diary" element={<Diary />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
