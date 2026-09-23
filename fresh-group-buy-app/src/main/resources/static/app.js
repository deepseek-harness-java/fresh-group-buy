const state = { products: [], categories: [], category: '全部', pickupPoints: [], selectedProduct: null };
const $ = selector => document.querySelector(selector);
const money = value => `¥${Number(value || 0).toFixed(2)}`;

async function request(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok || result.code !== 0) throw new Error(result.message || '请求失败');
  return result.data;
}

function showToast(message) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2400);
}

function esc(value) {
  return String(value ?? '').replace(/[&<>"']/g, item => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[item]));
}

function escapeHtml(value) {
  return String(value ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function inlineMd(value) {
  return escapeHtml(value)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>');
}

function renderMd(value) {
  const lines = String(value ?? '').split('\n');
  let html = '';
  let list = [];
  const flushList = () => {
    if (list.length) {
      html += `<ul>${list.map(item => `<li>${inlineMd(item)}</li>`).join('')}</ul>`;
      list = [];
    }
  };
  for (const raw of lines) {
    const line = raw.trim();
    const item = line.match(/^[-*]\s+(.*)$/);
    if (item) { list.push(item[1]); continue; }
    flushList();
    if (!line) continue;
    if (/^#{1,3}\s+/.test(line)) { html += `<h4>${inlineMd(line.replace(/^#+\s+/, ''))}</h4>`; continue; }
    html += `<p>${inlineMd(line)}</p>`;
  }
  flushList();
  return html || '<p></p>';
}

function progressPercent(product) {
  return Math.min(100, Math.round(product.currentParticipants * 100 / product.minGroupSize));
}

function renderCategories() {
  const categories = ['全部', ...new Set(state.products.map(item => item.category))];
  $('#categoryList').innerHTML = categories.map(item => `
    <button data-category="${esc(item)}" class="${state.category === item ? 'active' : ''}">${esc(item)}</button>
  `).join('');
}

function renderProducts() {
  const keyword = $('#searchInput').value.trim().toLowerCase();
  const items = state.products.filter(product => {
    const categoryOk = state.category === '全部' || product.category === state.category;
    const keywordOk = !keyword || `${product.name}${product.description}${product.tags.join('')}`.toLowerCase().includes(keyword);
    return categoryOk && keywordOk;
  });
  $('#productGrid').innerHTML = items.map(product => `
    <article class="product" data-product="${esc(product.id)}">
      <div class="cover">${esc(product.emoji)}</div>
      <div class="body">
        <div class="category">${esc(product.category)} · ${esc(product.unit)}</div>
        <h3>${esc(product.name)}</h3>
        <div class="tagline">${esc(product.tagline)}</div>
        <div class="price-row"><span class="price">${money(product.price)} <small>${esc(product.unit)}</small></span><span class="original">${money(product.originalPrice)}</span></div>
        <div class="group-row">
          <div class="progress"><i style="width:${progressPercent(product)}%"></i></div>
          <div class="group-meta"><span>${product.currentParticipants}/${product.minGroupSize} 人</span><span>库存 ${product.stock}</span></div>
        </div>
      </div>
    </article>
  `).join('');
}

function renderPickupPoints() {
  $('#pickupGrid').innerHTML = state.pickupPoints.map(point => `
    <article class="pickup">
      <h3>${esc(point.name)}</h3>
      <p>${esc(point.address)}</p>
      <p>${esc(point.contact)} · ${point.distanceKm}km · ${point.openTime}-${point.closeTime}</p>
      <div class="pickup-tags">${point.tags.map(tag => `<span>${esc(tag)}</span>`).join('')}</div>
    </article>
  `).join('');
}

async function loadInitialData() {
  state.products = await request('/api/groupbuy/products');
  state.pickupPoints = await request('/api/groupbuy/pickup-points');
  $('#statProducts').textContent = state.products.length;
  renderCategories();
  renderProducts();
  renderPickupPoints();
}

function openProductModal(product) {
  state.selectedProduct = product;
  $('#productModalCard').innerHTML = `
    <h3>${esc(product.emoji)} ${esc(product.name)}</h3>
    <p>${esc(product.description)}</p>
    <div class="form-grid">
      <div class="field"><label>数量</label><input id="orderQuantity" type="number" min="1" max="10" value="1"></div>
      <div class="field"><label>自提点</label><select id="pickupSelect">${state.pickupPoints.map(point => `<option value="${esc(point.id)}">${esc(point.name)} · ${point.distanceKm}km</option>`).join('')}</select></div>
    </div>
    <div class="modal-actions">
      <button class="secondary-btn" data-close>取消</button>
      <button class="primary-btn" id="placeOrderBtn">立即参团</button>
    </div>`;
  $('#productModal').classList.add('open');
}

function closeModal() {
  $('#productModal').classList.remove('open');
}

async function placeOrder() {
  if (!state.selectedProduct) return;
  try {
    const order = await request('/api/groupbuy/orders', {
      method: 'POST',
      body: JSON.stringify({
        customerId: 'customer-1',
        productId: state.selectedProduct.id,
        quantity: Number($('#orderQuantity').value),
        pickupPointId: $('#pickupSelect').value
      })
    });
    closeModal();
    showToast(`下单成功，取货码 ${order.pickupCode}`);
    await loadInitialData();
  } catch (error) {
    showToast(error.message);
  }
}

async function loadOrders() {
  const orders = await request('/api/groupbuy/orders?customerId=customer-1');
  showToast(orders.length ? `你有 ${orders.length} 个团购订单` : '暂无订单');
}

function openAssistant() { $('#assistantModal').classList.add('open'); }
function closeAssistant() { $('#assistantModal').classList.remove('open'); }

function appendMessage(role, content) {
  const element = document.createElement('div');
  element.className = `message ${role}`;
  element.innerHTML = role === 'assistant'
    ? `<div class="agent-avatar small">鲜</div><div class="chat-body"><div class="bubble"></div></div>`
    : `<div class="chat-body"><div class="bubble"></div></div>`;
  $('#assistantMessages').appendChild(element);
  const bubble = element.querySelector('.bubble');
  bubble.textContent = content;
  $('#assistantMessages').scrollTop = $('#assistantMessages').scrollHeight;
  return bubble;
}

async function askAssistant(message) {
  if (!message.trim()) return;
  openAssistant();
  appendMessage('user', message);
  const bubble = appendMessage('assistant', '');
  let answer = '';
  const response = await fetch('/api/assistant/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message })
  });
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const chunks = buffer.split(/\n\n/);
    buffer = chunks.pop();
    chunks.forEach(chunk => {
      let event = '';
      let data = '';
      chunk.split('\n').forEach(line => {
        if (line.startsWith('event:')) event = line.slice(6).trim();
        if (line.startsWith('data:')) data += line.slice(5).trim();
      });
      if (!data) return;
      try {
        const payload = JSON.parse(data);
        if (event === 'chunk' && payload.content) answer += payload.content;
        if (event === 'error' && payload.content) answer += payload.content;
        bubble.classList.add('md');
        bubble.innerHTML = answer ? renderMd(answer) : '正在想…';
        $('#assistantMessages').scrollTop = $('#assistantMessages').scrollHeight;
      } catch (error) {
        bubble.textContent = '助手连接中断。';
      }
    });
  }
}

function bindEvents() {
  document.addEventListener('click', event => {
    const product = event.target.closest('[data-product]');
    if (product) {
      const target = state.products.find(item => item.id === product.dataset.product);
      if (target) openProductModal(target);
    }
    if (event.target.closest('[data-category]')) {
      state.category = event.target.closest('[data-category]').dataset.category;
      renderCategories();
      renderProducts();
    }
    if (event.target.closest('[data-close]')) closeModal();
    if (event.target.closest('[data-assistant-close]')) closeAssistant();
    const prompt = event.target.closest('[data-prompt]');
    if (prompt) askAssistant(prompt.dataset.prompt);
  });
  $('#searchInput').addEventListener('input', renderProducts);
  $('#pickupBtn').addEventListener('click', () => $('#pickupGrid').scrollIntoView({ behavior: 'smooth' }));
  $('#ordersBtn').addEventListener('click', () => loadOrders().catch(error => showToast(error.message)));
  $('#exploreBtn').addEventListener('click', () => $('#productGrid').scrollIntoView({ behavior: 'smooth' }));
  $('#askAssistantBtn').addEventListener('click', openAssistant);
  $('#assistantFab').addEventListener('click', openAssistant);
  $('#placeOrderBtn').addEventListener('click', () => placeOrder().catch(error => showToast(error.message)));
  $('#productModal').addEventListener('click', event => { if (event.target.id === 'productModal') closeModal(); });
  $('#assistantModal').addEventListener('click', event => { if (event.target.id === 'assistantModal') closeAssistant(); });
  $('#assistantForm').addEventListener('submit', event => {
    event.preventDefault();
    const input = $('#assistantInput');
    askAssistant(input.value);
    input.value = '';
  });
}

async function init() {
  bindEvents();
  try { await loadInitialData(); } catch (error) { showToast(error.message); }
}

init();
