import { useEffect, useMemo, useState } from 'react';
import { createReview, getOrder, getReviewReply } from '../api/customerApi.js';
import './ReviewWrite.css';

function money(value) {
  if (value == null) return '-';
  return `${Number(value || 0).toLocaleString()}원`;
}

function formatDate(value) {
  if (!value) return '-';
  return String(value).replace('T', ' ').substring(0, 16);
}

function readCachedReceipt(orderId) {
  try {
    const cached = sessionStorage.getItem(`receipt:${orderId}`);
    return cached ? JSON.parse(cached) : null;
  } catch {
    return null;
  }
}

export default function ReviewWrite() {
  const orderId = useMemo(
    () => new URLSearchParams(window.location.search).get('orderId'),
    []
  );

  const [order, setOrder] = useState(null);
  const [content, setContent] = useState('');
  const [resultMsg, setResultMsg] = useState(null);
  const [savedReview, setSavedReview] = useState(null);
  const [reply, setReply] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isReplyLoading, setIsReplyLoading] = useState(false);

  useEffect(() => {
    if (!orderId) return;

    const cached = readCachedReceipt(orderId);

    if (cached) {
      setOrder(cached);
      return;
    }

    getOrder(orderId)
      .then(setOrder)
      .catch(() => {
        // QR로 들어온 모바일 사용자는 sessionStorage가 없을 수 있음.
        // 백엔드 GET 주문 조회가 아직 없더라도 리뷰 작성은 가능하게 최소 주문번호만 유지.
        setOrder({
          orderId: Number(orderId),
          orderDetails: []
        });
      });
  }, [orderId]);

  async function submitReview() {
    if (!orderId) {
      setResultMsg({ type: 'msg-error', text: '주문 정보가 없습니다.' });
      return;
    }

    if (!content.trim()) {
      setResultMsg({ type: 'msg-warning', text: '리뷰 내용을 입력해주세요.' });
      return;
    }

    try {
      setIsSubmitting(true);
      setResultMsg(null);

      const saved = await createReview({
        orderId,
        reviewContent: content.trim()
      });

      setSavedReview(saved);
    } catch (error) {
      console.error('리뷰 등록 실패:', error);
      setResultMsg({
        type: 'msg-error',
        text: error.message || '리뷰 등록에 실패했습니다.'
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  async function loadReply() {
    const reviewId = savedReview?.reviewId;

    if (!reviewId) {
      setReply({
        hasReply: false,
        message: '리뷰 저장 정보를 찾을 수 없습니다.'
      });
      return;
    }

    try {
      setIsReplyLoading(true);

      const data = await getReviewReply(reviewId);
      setReply(data);
    } catch (error) {
      console.error('답글 조회 실패:', error);
      setReply({
        hasReply: false,
        message: error.message || '아직 등록된 답글이 없습니다.'
      });
    } finally {
      setIsReplyLoading(false);
    }
  }

  function goBack() {
    if (window.history.length > 1) {
      window.history.back();
      return;
    }

    window.location.href = '/pos';
  }

  const menus = (order?.orderDetails || [])
    .map((d) => `${d.menuName} ${d.quantity}잔`)
    .join(', ');

  return (
    <main className="mobile-stage">
      <section className="phone-screen">
        <header className="app-topbar">
          <button type="button" className="back-btn" onClick={goBack}>
            ‹
          </button>

          <div className="brand">
            <div className="brand-icon">☕</div>
            <div>
              <strong>Smart Cafe</strong>
              <span>CafeOS</span>
            </div>
          </div>

          <button type="button" className="bell-btn">
            ⌕
          </button>
        </header>

        <section className="hero">
          <div className="hero-icon">💬</div>
          <h1>주문은 어떠셨나요?</h1>
          <p>남겨주신 리뷰는 해당 주문에 자동으로 저장됩니다.</p>
        </section>

        <section className="order-card">
          <div className="order-row">
            <div className="row-icon">#</div>
            <div className="row-copy">
              <span>주문번호</span>
              <strong>{order?.orderId || orderId || '-'}</strong>
            </div>
          </div>

          <div className="order-row">
            <div className="row-icon">⌚</div>
            <div className="row-copy">
              <span>주문일시</span>
              <strong>{formatDate(order?.createdAt)}</strong>
            </div>
          </div>

          <div className="order-row">
            <div className="row-icon">☕</div>
            <div className="row-copy">
              <span>주문 메뉴</span>
              <strong>{menus || '-'}</strong>
            </div>
          </div>

          <div className="order-row total">
            <div className="row-icon">₩</div>
            <div className="row-copy">
              <span>총 결제금액</span>
              <strong>{money(order?.orderPrice)}</strong>
            </div>
          </div>
        </section>

        {!savedReview ? (
          <section className="review-card">
            <div className="section-title">
              <h2>리뷰 내용</h2>
              <span>{content.length} / 500</span>
            </div>

            <textarea
              maxLength={500}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder={'카페 이용 경험을 자유롭게 남겨주세요.\n맛, 서비스, 분위기 등 어떤 내용도 좋아요!'}
            />

            <div className="chip-grid review-chips">
              {['커피가 맛있어요', '직원이 친절해요', '매장이 깔끔해요', '다시 방문할래요'].map((chip) => (
                <button
                  key={chip}
                  type="button"
                  className="chip brown"
                  onClick={() => setContent((prev) => `${prev}${prev ? ' ' : ''}${chip}`)}
                >
                  {chip}
                </button>
              ))}
            </div>

            <div className="notice">
              <span>i</span>
              주문 건당 리뷰는 1회만 작성할 수 있습니다.
            </div>

            {resultMsg && (
              <div className={`result-msg ${resultMsg.type}`}>
                {resultMsg.text}
              </div>
            )}

            <button
              type="button"
              className="submit-btn"
              disabled={isSubmitting}
              onClick={submitReview}
            >
              {isSubmitting ? '등록 중...' : '리뷰 등록'}
            </button>

            <button type="button" className="later-btn" onClick={goBack}>
              나중에 작성
            </button>
          </section>
        ) : (
          <section className="review-card done-section visible">
            <div className="done-header">
              <div className="done-icon">✅</div>
              <h2>리뷰가 등록되었습니다!</h2>
              <p>소중한 의견 감사드립니다.</p>
            </div>

            <div className="done-content-box">
              <p className="done-content-label">내가 작성한 리뷰</p>
              <p className="done-content-text">{savedReview.reviewContent || content}</p>
            </div>

            <button
              type="button"
              className="later-btn done-close-btn"
              onClick={loadReply}
              disabled={isReplyLoading}
            >
              {isReplyLoading ? '답글 확인 중...' : '사장님 답글 확인'}
            </button>

            {reply && (
              <div className="reply-result-card">
                <span>{reply.hasReply ? '답글 도착' : '답글 대기'}</span>
                <p>{reply.hasReply ? reply.replyContent : reply.message || '아직 등록된 답글이 없습니다.'}</p>
                {reply.repliedAt && <small>{reply.repliedAt}</small>}
              </div>
            )}
          </section>
        )}

        <div className="home-indicator" />
      </section>
    </main>
  );
}