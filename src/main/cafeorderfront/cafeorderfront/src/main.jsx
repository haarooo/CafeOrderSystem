import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';

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

        {/* 잘못된 프론트 경로로 들어오면 POS로 이동 */}
        <Route path="*" element={<Navigate to="/pos" replace />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);