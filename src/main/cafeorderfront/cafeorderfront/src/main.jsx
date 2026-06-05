import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import TabletPOS from './pages/TabletPOS.jsx';
import ReceiptPopup from './pages/ReceiptPopup.jsx';
import ReviewWrite from './pages/ReviewWrite.jsx';
import './styles.css';

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/pos" replace />} />
        <Route path="/pos" element={<TabletPOS />} />
        <Route path="/receipt" element={<ReceiptPopup />} />
        <Route path="/review/write" element={<ReviewWrite />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);
