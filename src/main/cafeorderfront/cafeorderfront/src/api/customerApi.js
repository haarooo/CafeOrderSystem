const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });

  const contentType = res.headers.get('content-type') || '';
  const body = contentType.includes('application/json') ? await res.json() : await res.text();

  if (!res.ok) {
    const message = typeof body === 'object' ? body.message || JSON.stringify(body) : body;
    throw new Error(message || `요청 실패 (${res.status})`);
  }

  return body;
}

// 구매 서버가 사장 서버에서 메뉴를 가져와 React POS에 전달한다.
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
export function getOrder(orderId) {
  return request(`/api/orders/${orderId}`);
}

// 리뷰 작성
export function createReview({ orderId, reviewContent }) {
  return request('/api/reviews', {
    method: 'POST',
    body: JSON.stringify({ orderId: Number(orderId), reviewContent })
  });
}

// 사장 답글 조회
export function getReviewReply(reviewId) {
  return request(`/api/reviews/${reviewId}/reply`);
}