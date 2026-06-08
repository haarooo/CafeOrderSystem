import { useEffect, useMemo, useState } from 'react';
import { createOrder, getMenus } from '../api/customerApi.js';
import './TabletPOS.css';

function money(value) {
  return `${Number(value || 0).toLocaleString()}원`;
}

function normalizeImageUrl(value) {
  if (!value) return null;

  // 이미 절대 URL이면 그대로 사용
  if (String(value).startsWith('http://') || String(value).startsWith('https://')) {
    return value;
  }

  // Spring Boot 같은 도메인 기준 상대경로
  return value;
}

// 구매 서버 /api/menus 응답을 POS 화면에서 쓰는 형태로 변환
function normalizeMenu(menu) {
  return {
    menuId: menu.menuId,
    menuName: menu.menuName,
    price: menu.menuPrice,

    // 구매 서버가 만들어준 이미지 프록시 URL을 우선 사용
    imageUrl: normalizeImageUrl(menu.imageUrl || menu.menuImage),

    // 구매 서버에 카테고리가 없으면 전체로 묶는다.
    category: menu.menuCategory || menu.category || '전체'
  };
}

export default function TabletPOS() {
  const [menus, setMenus] = useState([]);
  const [activeCategory, setActiveCategory] = useState('전체');
  const [cart, setCart] = useState([]);
  const [isPaying, setIsPaying] = useState(false);
  const [isMenuLoading, setIsMenuLoading] = useState(true);
  const [menuLoadError, setMenuLoadError] = useState(null);

  useEffect(() => {
    loadMenus();
  }, []);

  async function loadMenus() {
    try {
      setIsMenuLoading(true);
      setMenuLoadError(null);

      const data = await getMenus();

      const normalizedMenus = Array.isArray(data)
        ? data
            .map(normalizeMenu)
            .filter((menu) => menu.menuId && menu.menuName)
        : [];

      setMenus(normalizedMenus);

      if (normalizedMenus.length > 0) {
        setActiveCategory(normalizedMenus[0].category || '전체');
      } else {
        setActiveCategory('전체');
      }
    } catch (error) {
      console.error('메뉴 목록 조회 실패:', error);
      setMenuLoadError('메뉴를 불러오지 못했습니다. 구매 서버와 사장 서버 연결을 확인해주세요.');
      setMenus([]);
      setActiveCategory('전체');
    } finally {
      setIsMenuLoading(false);
    }
  }

  const categories = useMemo(() => {
    const uniqueCategories = menus
      .map((menu) => menu.category || '전체')
      .filter(Boolean);

    const result = [...new Set(uniqueCategories)];

    return result.length > 0 ? result : ['전체'];
  }, [menus]);

  const filteredMenus = useMemo(
    () => menus.filter((menu) => menu.category === activeCategory),
    [menus, activeCategory]
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
          item.menuId === menu.menuId
            ? { ...item, quantity: item.quantity + 1 }
            : item
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
    setCart((prev) =>
      prev.map((item) =>
        item.menuId === menuId
          ? { ...item, quantity: item.quantity + 1 }
          : item
      )
    );
  }

  async function handlePay() {
    if (cart.length === 0) {
      alert('장바구니에 상품을 담아주세요.');
      return;
    }

    try {
      setIsPaying(true);

      // 주문 생성에는 menuId, quantity만 보낸다.
      const items = cart.map(({ menuId, quantity }) => ({ menuId, quantity }));
      const order = await createOrder(items);

      if (!order?.orderId) {
        throw new Error('주문번호가 응답에 없습니다.');
      }

      // 영수증 화면에서 보여줄 주문 상세는 현재 장바구니 기준으로 임시 저장
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
      console.error('결제 처리 실패:', error);
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
            <span>사장 서버에 등록된 메뉴를 불러와 주문 화면에 표시합니다.</span>
          </div>

          <div className="today-order-card">
            <span>오늘 주문</span>
            <strong>
              {cart.length}<small>건</small>
            </strong>
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
              <em>{isMenuLoading ? '메뉴 불러오는 중' : '터치 주문'}</em>
            </div>

            {menuLoadError && (
              <div className="empty-cart" style={{ marginBottom: '16px' }}>
                {menuLoadError}
              </div>
            )}

            <div className="tablet-product-grid">
              {isMenuLoading ? (
                <div className="empty-cart">메뉴를 불러오는 중입니다.</div>
              ) : filteredMenus.length === 0 ? (
                <div className="empty-cart">등록된 메뉴가 없습니다.</div>
              ) : (
                filteredMenus.map((menu) => (
                  <button
                    key={menu.menuId}
                    className="tablet-product-card"
                    onClick={() => addToCart(menu)}
                  >
                    <div className="p-thumb">
                      {menu.imageUrl ? (
                        <img
                          src={menu.imageUrl}
                          alt={menu.menuName}
                          style={{
                            width: '100%',
                            height: '100%',
                            objectFit: 'cover',
                            borderRadius: '18px'
                          }}
                        />
                      ) : (
                        <span>☕</span>
                      )}
                    </div>

                    <strong>{menu.menuName}</strong>
                    <span>{money(menu.price)}</span>
                  </button>
                ))
              )}
            </div>
          </section>

          <aside className="tablet-cart">
            <div className="cart-head">
              <div>
                <h2>장바구니</h2>
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
                      <span>
                        {money(item.price)} · {item.quantity}잔
                      </span>
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