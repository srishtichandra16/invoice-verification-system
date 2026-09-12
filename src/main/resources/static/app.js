(() => {
  'use strict';
  const form = document.getElementById('invoice-form');
  const fields = document.getElementById('form-fields');
  const upload = document.getElementById('invoiceImage');
  const zone = document.getElementById('upload-zone');
  const fileError = document.getElementById('file-error');
  const selection = document.getElementById('file-selection');
  const error = document.getElementById('submit-error');
  const button = document.getElementById('submit-button');
  const status = document.getElementById('submission-status');
  const success = document.getElementById('success');
  const allowedTypes = new Set(['application/pdf', 'image/jpeg', 'image/png']);
  const maxFileBytes = 10 * 1024 * 1024;
  const pathParts = window.location.pathname.split('/').filter(Boolean);
  const brandSlug = pathParts.length >= 2 && pathParts[1] === 'register' ? pathParts[0] : 'aigner';
  let brandName = brandSlug.toUpperCase();
  let submitting = false;

  async function loadBrand() {
    try {
      const response = await fetch(`/api/public/brands/${encodeURIComponent(brandSlug)}`, { headers: { Accept: 'application/json' } });
      if (!response.ok) throw new Error();
      const brand = await response.json();
      brandName = brand.displayName;
      document.title = `${brandName} · Invoice registration`;
      document.querySelectorAll('[data-brand-name]').forEach(node => { node.textContent = brandName; });
    } catch {
      showError('This registration link is unavailable. Please use the link provided by the brand.');
      button.disabled = true;
    }
  }
  loadBrand();

  function showFileError(message) {
    fileError.textContent = message;
    fileError.hidden = !message;
    upload.setAttribute('aria-invalid', String(Boolean(message)));
  }

  function updateFile() {
    const file = upload.files[0];
    showFileError('');
    selection.hidden = true;
    if (!file) return false;
    let message = '';
    if (!allowedTypes.has(file.type)) message = 'Please choose a PDF, JPG or PNG document.';
    else if (file.size === 0) message = 'This file is empty. Please choose your invoice document.';
    else if (file.size > maxFileBytes) message = 'This file is larger than 10 MB. Please choose a smaller document.';
    if (message) {
      upload.value = '';
      showFileError(message);
      return false;
    }
    const size = file.size < 1024 * 1024
      ? `${Math.max(1, Math.round(file.size / 1024))} KB`
      : `${(file.size / (1024 * 1024)).toFixed(1)} MB`;
    document.getElementById('file-description').textContent = `${file.name} · ${size}`;
    selection.hidden = false;
    return true;
  }

  upload.addEventListener('change', updateFile);
  upload.addEventListener('invalid', () => {
    if (!fileError.textContent) showFileError('Please attach your invoice document.');
  });
  document.getElementById('remove-file').addEventListener('click', () => {
    upload.value = '';
    selection.hidden = true;
    showFileError('');
    upload.focus();
  });
  for (const eventName of ['dragenter', 'dragover']) {
    zone.addEventListener(eventName, event => {
      event.preventDefault();
      if (!submitting) zone.classList.add('drag-over');
    });
  }
  zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
  zone.addEventListener('drop', event => {
    event.preventDefault();
    zone.classList.remove('drag-over');
    if (submitting) return;
    const files = event.dataTransfer?.files;
    if (!files || files.length !== 1) {
      showFileError('Please upload one invoice document at a time.');
      return;
    }
    upload.files = files;
    updateFile();
  });

  form.addEventListener('input', event => event.target.removeAttribute('aria-invalid'));

  function setBusy(busy) {
    submitting = busy;
    fields.disabled = busy;
    button.disabled = busy;
    form.setAttribute('aria-busy', String(busy));
    document.getElementById('submit-label').textContent = busy ? 'Submitting…' : 'Submit invoice';
    button.querySelector('.button-arrow').hidden = busy;
    button.querySelector('.spinner').hidden = !busy;
    status.textContent = busy ? 'Uploading your document. Please keep this page open.' : '';
  }

  function showError(message) {
    error.textContent = message;
    error.hidden = false;
    error.focus();
  }

  function responseError(response, data) {
    if (response.status === 413) return 'The upload is too large. Choose a document up to 10 MB.';
    if (response.status === 401 || response.status === 403) return 'The server did not allow this submission. Please contact the application administrator.';
    if (response.status >= 500) return 'The server could not complete your submission. Please check with support before submitting again.';
    return typeof data.message === 'string' && data.message
      ? data.message : `Your invoice could not be submitted (HTTP ${response.status}). Please check your details.`;
  }

  form.addEventListener('submit', async event => {
    event.preventDefault();
    if (submitting) return;
    if (!form.reportValidity() || !updateFile()) return;
    error.hidden = true;
    // Build the multipart body before disabling the form controls.
    const payload = new FormData(form);
    payload.set('marketingConsent', String(document.getElementById('marketingConsent').checked));
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 120000);
    setBusy(true);
    try {
      const response = await fetch(`/api/public/brands/${encodeURIComponent(brandSlug)}/invoices`, {
        method: 'POST',
        headers: { Accept: 'application/json' },
        body: payload,
        signal: controller.signal
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        if (data.fields && typeof data.fields === 'object') {
          for (const name of Object.keys(data.fields)) {
            form.elements.namedItem(name)?.setAttribute('aria-invalid', 'true');
          }
        }
        throw new Error(responseError(response, data));
      }
      if (data.invoiceId == null) throw new Error('The server responded, but no invoice ID was returned. Please check with support before submitting again.');
      document.getElementById('receipt-id').textContent = `#${data.invoiceId}`;
      document.getElementById('receipt-brand').textContent = data.brandSlug?.toUpperCase() ?? brandName;
      document.getElementById('receipt-status').textContent = data.verificationStatus === 'PENDING'
        ? 'Pending verification' : data.verificationStatus || 'Submitted';
      form.hidden = true;
      success.hidden = false;
      success.focus();
      success.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (failure) {
      const message = failure.name === 'AbortError'
        ? 'The request timed out. It may have reached the server. Please check with support before submitting again.'
        : failure instanceof TypeError
          ? 'The connection was interrupted. Your submission may have reached the server. Please check before submitting again.'
          : failure.message;
      showError(message);
    } finally {
      clearTimeout(timeout);
      setBusy(false);
    }
  });

  document.getElementById('start-again').addEventListener('click', () => {
    form.reset();
    for (const field of form.querySelectorAll('[aria-invalid]')) field.removeAttribute('aria-invalid');
    showFileError('');
    selection.hidden = true;
    error.hidden = true;
    success.hidden = true;
    form.hidden = false;
    document.querySelector('.additional').open = false;
    document.getElementById('purchaseDate').focus();
  });
})();
