import { useMemo, useState } from 'react';
import { createOrder } from '../api/customerApi.js';
import './TabletPOS.css';

const categories = ['커피', '라떼', '에이드', '티', '디저트', '기타'];

const menus = [
  { menuId: 1, category: '커피', menuName: '아메리카노', price: 3000 },
  { menuId: 2, category: '라떼', menuName: '카페라떼', price: 3800 },
  { menuId: 3, category: '라떼', menuName: '바닐라라떼', price: 4300 },
  { menuId: 4, category: '라떼', menuName: '카페모카', price: 4300 },
  { menuId: 5, category: '커피', menuName: '아메모카', price: 4000 },
  { menuId: 6, category: '라떼', menuName: '카라멜 마키아또', price: 4000 },
  { menuId: 7, category: '라떼', menuName: '초코라떼', price: 4000 },
  { menuId: 8, category: '에이드', menuName: '딸기 에이드', price: 4500 },
  { menuId: 9, category: '커피', menuName: '콜드브루', price: 4500 },
  { menuId: 10, category: '디저트', menuName: '버터 스콘', price: 3200 },
  { menuId: 11, category: '티', menuName: '얼그레이 티', price: 3500 },
  { menuId: 12, category: '기타', menuName: '생수', price: 1200 }
];

function money(value) {
  return `${Number(value || 0).toLocaleString()}원`;
}

export default function TabletPOS() {
  const [activeCategory, setActiveCategory] = useState('커피');
  const [cart, setCart] = useState([]);
  const [isPaying, setIsPaying] = useState(false);

  const filteredMenus = useMemo(
    () => menus.filter((menu) => menu.category === activeCategory),
    [activeCategory]
  );

  const total = useMemo(
    () => cart.reduce((sum, item) => sum + item.price * item.quantity, 0),
    [cart]
  );

  const totalQuantity = useMemo(
    () => cart.reduce((sum, item) => sum + item.quantity, 0),
    [cart]
  );

  function addToCart(menu) {
    setCart((prev) => {
      const exists = prev.find((item) => item.menuId === menu.menuId);
      if (exists) {
        return prev.map((item) =>
          item.menuId === menu.menuId ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      return [...prev, { ...menu, quantity: 1 }];
    });
  }

  function decrease(menuId) {
    setCart((prev) =>
      prev.flatMap((item) => {
        if (item.menuId !== menuId) return [item];
        if (item.quantity <= 1) return [];
        return [{ ...item, quantity: item.quantity - 1 }];
      })
    );
  }

  function increase(menuId) {
    setCart((prev) => prev.map((item) =>
      item.menuId === menuId ? { ...item, quantity: item.quantity + 1 } : item
    ));
  }

  async function handlePay() {
    if (cart.length === 0) {
      alert('장바구니에 상품을 담아주세요.');
      return;
    }

    try {
      setIsPaying(true);
      const items = cart.map(({ menuId, quantity }) => ({ menuId, quantity }));
      const order = await createOrder(items);
      const receipt = {
        ...order,
        orderDetails: cart.map((item) => ({
          menuId: item.menuId,
          menuName: item.menuName,
          menuPrice: item.price,
          quantity: item.quantity,
          totalPrice: item.price * item.quantity
        }))
      };

      sessionStorage.setItem(`receipt:${order.orderId}`, JSON.stringify(receipt));
      setCart([]);
      window.open(
        `/receipt?orderId=${order.orderId}`,
        `receipt_${order.orderId}`,
        'width=520,height=780,scrollbars=yes,resizable=yes'
      );
    } catch (error) {
      alert(`결제 처리 중 오류가 발생했습니다.\n${error.message}`);
    } finally {
      setIsPaying(false);
    }
  }

  return (
    <main className="tablet-pos-page">
      <section className="tablet-shell">
        <header className="tablet-header">
          <div>
            <p className="eyebrow">CafeOS · Tablet Order</p>
            <h1>주문을 받아볼까요?</h1>
            <span>카테고리를 고르고 메뉴를 터치하면 우측 장바구니에 담깁니다.</span>
          </div>
          <div className="today-order-card">
            <span>오늘 주문</span>
            <strong>142<small>건</small></strong>
          </div>
        </header>

        <div className="tablet-pos-layout">
          <aside className="tablet-categories">
            {categories.map((category) => (
              <button
                key={category}
                className={category === activeCategory ? 'active' : ''}
                onClick={() => setActiveCategory(category)}
              >
                {category}
              </button>
            ))}
          </aside>

          <section className="tablet-products">
            <div className="product-section-head">
              <div>
                <strong>{activeCategory}</strong>
                <span>{filteredMenus.length}개 메뉴</span>
              </div>
              <em>터치 주문</em>
            </div>

            <div className="tablet-product-grid">
              {filteredMenus.map((menu) => (
                <button
                  key={menu.menuId}
                  className="tablet-product-card"
                  onClick={() => addToCart(menu)}
                >
                  <div className="p-thumb"><span>☕</span></div>
                  <strong>{menu.menuName}</strong>
                  <span>{money(menu.price)}</span>
                </button>
              ))}
            </div>
          </section>

          <aside className="tablet-cart">
            <div className="cart-head">
              <div>
                <h2>주문 내역</h2>
                <p>테이블 5번 · {totalQuantity}개 담김</p>
              </div>
              <button onClick={() => setCart([])}>초기화</button>
            </div>

            <div className="cart-items">
              {cart.length === 0 ? (
                <div className="empty-cart">장바구니가 비어 있습니다.</div>
              ) : (
                cart.map((item) => (
                  <div className="tablet-cart-item" key={item.menuId}>
                    <div>
                      <strong>{item.menuName}</strong>
                      <span>{money(item.price)} · {item.quantity}잔</span>
                    </div>
                    <div className="qty-box">
                      <button onClick={() => decrease(item.menuId)}>-</button>
                      <em>{item.quantity}</em>
                      <button onClick={() => increase(item.menuId)}>+</button>
                    </div>
                    <b>{money(item.price * item.quantity)}</b>
                  </div>
                ))
              )}
            </div>

            <footer className="cart-foot">
              <div className="cart-total">
                <span>총 금액</span>
                <strong>{money(total)}</strong>
              </div>
              <button className="pay-btn" disabled={isPaying} onClick={handlePay}>
                {isPaying ? '처리 중...' : '결제하기'}
              </button>
            </footer>
          </aside>
        </div>
      </section>
    </main>
  );
}
