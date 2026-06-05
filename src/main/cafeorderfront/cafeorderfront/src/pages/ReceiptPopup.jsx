import { useEffect, useMemo, useState } from 'react';
import { getOrder } from '../api/customerApi.js';
import './ReceiptPopup.css';

function money(value) {
  return `${Number(value || 0).toLocaleString()}원`;
}

function formatDate(value) {
  if (!value) return '-';
  return String(value).replace('T', ' ').substring(0, 16);
}

export default function ReceiptPopup() {
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');

  const orderId = useMemo(() => new URLSearchParams(window.location.search).get('orderId'), []);

  useEffect(() => {
    if (!orderId) {
      setError('주문 정보를 찾을 수 없습니다.');
      return;
    }

    const cached = sessionStorage.getItem(`receipt:${orderId}`);
    if (cached) {
      setOrder(JSON.parse(cached));
      return;
    }

    getOrder(orderId)
      .then(setOrder)
      .catch(() => setError('주문 조회에 실패했습니다.'));
  }, [orderId]);

  const reviewUrl = order?.reviewPageUrl || `${window.location.origin}/review/write?orderId=${order?.orderId || orderId}`;

  function copyReviewUrl() {
    if (!reviewUrl) return;
    navigator.clipboard?.writeText(reviewUrl).then(() => alert('리뷰 URL이 복사되었습니다.'));
  }

  return (
    <main className="receipt-page">
      <section className="popup-shell">
        <div className="popup-titlebar">
          <div className="title-left">
            <span className="title-dot" />
            <span>디지털 영수증</span>
          </div>
          <button type="button" className="window-close" onClick={() => window.close()}>×</button>
        </div>

        <article className="receipt-card">
          <section className="brand-block">
            <div className="brand-icon">☕</div>
            <div className="brand-name">Smart Cafe</div>
            <div className="brand-sub">CafeOS Digital Receipt</div>
          </section>

          <section className="receipt-head">
            <h1>디지털 영수증</h1>
            <p>주문해 주셔서 감사합니다. 좋은 하루 되세요!</p>
          </section>

          {error ? (
            <div className="receipt-error">{error}</div>
          ) : (
            <>
              <section className="order-info">
                <div className="info-row"><span>주문번호</span><strong>{order?.orderId || '-'}</strong></div>
                <div className="info-row"><span>결제 일시</span><strong>{formatDate(order?.createdAt)}</strong></div>
                <div className="info-row"><span>결제 상태</span><strong><em className="paid-badge">결제 완료</em></strong></div>
              </section>

              <section className="receipt-items">
                <div className="item-header"><span>메뉴명</span><span>수량</span><span>금액</span></div>
                {(order?.orderDetails || []).length === 0 ? (
                  <div className="item-row empty"><span>주문 상세 준비 중</span><span>-</span><span>-</span></div>
                ) : (
                  order.orderDetails.map((item, idx) => (
                    <div className="item-row" key={`${item.menuId}-${idx}`}>
                      <span>{item.menuName}</span>
                      <span>{item.quantity}</span>
                      <span>{money(item.totalPrice || item.menuPrice * item.quantity)}</span>
                    </div>
                  ))
                )}
              </section>

              <section className="total-box">
                <div className="total-line"><span>결제 금액</span><strong>{money(order?.orderPrice)}</strong></div>
                <div className="total-line muted"><span>할인</span><strong>0원</strong></div>
                <div className="total-final"><span>총 결제금액</span><strong>{money(order?.orderPrice)}</strong></div>
              </section>

              <section className="qr-section">
                <div className="qr-box">
                  {order?.qrUrl ? <img className="qr-img" src={order.qrUrl} alt="리뷰 QR" /> : <div className="qr-placeholder">QR<br/>준비 중</div>}
                </div>
                <div className="qr-copy">
                  <h2>리뷰를 남겨주세요</h2>
                  <p>QR을 스캔하면 모바일 리뷰 작성 화면으로 이동합니다.</p>
                  <div className="url-preview">{reviewUrl}</div>
                </div>
              </section>

              <div className="receipt-actions">
                <button className="btn primary" onClick={() => window.open(reviewUrl, '_blank')}>리뷰 작성 열기</button>
                <button className="btn secondary" onClick={copyReviewUrl}>URL 복사</button>
              </div>
            </>
          )}
        </article>
      </section>
    </main>
  );
}
