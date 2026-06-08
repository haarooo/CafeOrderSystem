/*
 * CafeOrderSystem React API Client
 *
 * 통합 배포 구조:
 * - React 정적 파일과 Spring Boot API가 같은 도메인에서 동작
 * - 따라서 기본 API_BASE는 빈 문자열('')
 *
 * 예:
 * 브라우저 → http://cafeordersystem.../pos
 * React API → /api/menus
 * 실제 요청 → http://cafeordersystem.../api/menus
 */

const RAW_API_BASE = import.meta.env.VITE_API_BASE_URL || '';

function normalizeBaseUrl(value) {
  if (!value) return '';
  return String(value).replace(/\/$/, '');
}

const API_BASE = normalizeBaseUrl(RAW_API_BASE);

async function request(path, options = {}) {
  const url = `${API_BASE}${path}`;

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(options.headers || {})
  };

  const res = await fetch(url, {
    ...options,
    headers
  });

  const contentType = res.headers.get('content-type') || '';

  let body = null;

  if (contentType.includes('application/json')) {
    body = await res.json();
  } else {
    body = await res.text();
  }

  if (!res.ok) {
    const message =
      typeof body === 'object' && body !== null
        ? body.message || body.error || JSON.stringify(body)
        : body;

    throw new Error(message || `요청 실패 (${res.status})`);
  }

  return body;
}

// 구매 서버가 menu_read 테이블 기준으로 메뉴를 내려준다.
export function getMenus() {
  return request('/api/menus');
}

// 주문 생성
export function createOrder(items) {
  return request('/api/orders', {
    method: 'POST',
    body: JSON.stringify({ items })
  });
}

// 주문 조회
// 현재 백엔드에 GET /api/orders/{orderId}가 없다면 fallback 처리됨.
export function getOrder(orderId) {
  return request(`/api/orders/${orderId}`);
}

// 리뷰 작성
export function createReview({ orderId, reviewContent }) {
  return request('/api/reviews', {
    method: 'POST',
    body: JSON.stringify({
      orderId: Number(orderId),
      reviewContent
    })
  });
}

// 사장 답글 조회
export function getReviewReply(reviewId) {
  return request(`/api/reviews/${reviewId}/reply`);
}