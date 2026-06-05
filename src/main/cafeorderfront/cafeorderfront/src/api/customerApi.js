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

export function createOrder(items) {
  return request('/api/orders', {
    method: 'POST',
    body: JSON.stringify({ items })
  });
}

export function getOrder(orderId) {
  return request(`/api/orders/${orderId}`);
}

export function createReview({ orderId, reviewContent }) {
  return request('/api/reviews', {
    method: 'POST',
    body: JSON.stringify({ orderId: Number(orderId), reviewContent })
  });
}

export function getReviewReply(reviewId) {
  return request(`/api/reviews/${reviewId}/reply`);
}
