(() => {
  'use strict';
  const parts = location.pathname.split('/').filter(Boolean);
  const brandSlug = parts.length >= 2 && parts[1] === 'admin' ? parts[0] : '';
  const login = document.getElementById('login-panel'), dashboard = document.getElementById('dashboard');
  let authorization = '';
  let loadedInvoices = [];
  const request = async (path, options = {}) => {
    const response = await fetch(path, { ...options, headers: { Accept: 'application/json', Authorization: authorization, ...(options.headers || {}) } });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.message || (response.status === 401 ? 'Incorrect email or password.' : 'Request could not be completed.'));
    return body;
  };
  const show = (id, message) => { const el = document.getElementById(id); el.textContent = message; el.hidden = !message; };
  function renderInvoices(invoices) {
    const list = document.getElementById('invoice-list'); list.replaceChildren();
    if (!invoices.length) { list.textContent = 'No matching invoices.'; return; }
    invoices.forEach(invoice => {
      const row = document.createElement('article'); row.className = 'admin-invoice';
      const content = document.createElement('div');
      const heading = document.createElement('h2'); heading.textContent = invoice.invoiceNumber || `Invoice #${invoice.id}`;
      const detail = document.createElement('p'); detail.textContent = `${invoice.name || 'Customer not supplied'} · ${invoice.purchaseDate || 'No purchase date'} · ${invoice.verificationStatus}`;
      const note = document.createElement('input'); note.placeholder = 'Optional review note'; note.maxLength = 1000; note.setAttribute('aria-label', `Review note for invoice ${invoice.id}`);
      const select = document.createElement('select'); select.setAttribute('aria-label', `Status for invoice ${invoice.id}`);
      ['PENDING', 'VERIFIED', 'FAILED'].forEach(status => select.add(new Option(status, status, false, status === invoice.verificationStatus)));
      select.addEventListener('change', async () => { try { const updated = await request(`/api/admin/invoices/${invoice.id}/status`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ verificationStatus: select.value, reviewNote: note.value }) }); invoice.verificationStatus = updated.verificationStatus; note.value = ''; show('dashboard-error', ''); } catch (error) { show('dashboard-error', error.message); select.value = invoice.verificationStatus; } });
      const documentButton = document.createElement('button'); documentButton.type = 'button'; documentButton.textContent = 'Download invoice';
      documentButton.addEventListener('click', async () => { try { const result = await request(`/api/admin/invoices/${invoice.id}/document-url`); location.href = result.url; } catch (error) { show('dashboard-error', error.message); } });
      content.append(heading, detail, note); row.append(content, select, documentButton); list.append(row);
    });
  }
  async function loadDashboard() {
    try {
      const [summary, page] = await Promise.all([request('/api/admin/dashboard'), request('/api/admin/invoices?size=50')]);
      document.getElementById('invoice-count').textContent = summary.invoiceCount;
      loadedInvoices = page.content || []; renderInvoices(loadedInvoices);
    } catch (error) { show('dashboard-error', error.message); }
  }
  document.getElementById('login-form').addEventListener('submit', async event => {
    event.preventDefault(); show('login-error', '');
    authorization = `Basic ${btoa(`${document.getElementById('email').value}:${document.getElementById('password').value}`)}`;
    try {
      const me = await request('/api/admin/me');
      if (me.brandSlug !== brandSlug) throw new Error('This account belongs to a different brand. Use that brand’s admin link.');
      document.getElementById('brand-name').textContent = me.brandName; document.getElementById('admin-email').textContent = me.email;
      login.hidden = true; dashboard.hidden = false; await loadDashboard();
    } catch (error) { authorization = ''; show('login-error', error.message); }
  });
  document.getElementById('refresh').addEventListener('click', loadDashboard);
  document.getElementById('invoice-search').addEventListener('input', event => {
    const query = event.target.value.trim().toLowerCase();
    renderInvoices(loadedInvoices.filter(invoice => !query || [invoice.invoiceNumber, invoice.referenceNumber, invoice.name, invoice.email].some(value => value?.toLowerCase().includes(query))));
  });
  document.getElementById('logout').addEventListener('click', () => {
    authorization = ''; loadedInvoices = []; document.getElementById('password').value = '';
    dashboard.hidden = true; login.hidden = false; document.getElementById('email').focus();
  });
})();
