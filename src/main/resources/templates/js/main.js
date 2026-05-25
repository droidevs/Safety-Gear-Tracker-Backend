/**
 * Safety Gear Tracker — Main JS
 * Handles: JWT cookie auth, API requests, live camera feed, flash messages
 */
'use strict';

// ── API helpers ────────────────────────────────────────────────

const API = '/api/v1';

async function apiFetch(path, options = {}) {
    const res = await fetch(API + path, {
        credentials: 'include',  // send jwt cookie
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
        ...options,
    });

    if (res.status === 401) {
        // Redirect to login if unauthorized
        window.location.href = '/ui/login?expired=1';
        return null;
    }

    return res;
}

async function apiJson(path, options = {}) {
    const res = await apiFetch(path, options);
    if (!res) return null;
    if (!res.ok) {
        const err = await res.json().catch(() => ({ error: res.statusText }));
        throw err;
    }
    return res.json();
}

// ── Flash messages ─────────────────────────────────────────────

function showFlash(msg, type = 'info') {
    const el = document.createElement('div');
    el.className = `flash flash-${type}`;
    el.textContent = msg;
    const main = document.querySelector('.main') || document.body;
    main.insertBefore(el, main.firstChild);
    setTimeout(() => el.remove(), 5000);
}

// ── Auth ───────────────────────────────────────────────────────

async function login(email, password) {
    const res = await fetch(API + '/auth/authenticate', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
        throw new Error(data.businessErrorDescription || data.error || 'Login failed');
    }
    return data;
}

async function masterLogin(email, password) {
    const res = await fetch(API + '/auth/master/login', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.businessErrorDescription || data.error || 'Master login failed');
    return data;
}

async function logout() {
    await apiFetch('/auth/logout', { method: 'POST' });
    window.location.href = '/ui/login';
}

// ── Login page ─────────────────────────────────────────────────

function initLoginPage() {
    const form = document.getElementById('login-form');
    if (!form) return;

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const email    = form.email.value.trim();
        const password = form.password.value;
        const isMaster = form.querySelector('#master-toggle')?.checked;
        const btn      = form.querySelector('[type=submit]');
        const errEl    = document.getElementById('login-error');

        btn.disabled = true;
        btn.textContent = 'Authenticating…';
        errEl.textContent = '';

        try {
            const loginFn = isMaster ? masterLogin : login;
            const data = await loginFn(email, password);

            // Store token in memory as well (cookie is already set by server)
            if (data.token) {
                sessionStorage.setItem('sgt_token', data.token);
            }
            window.location.href = '/ui/dashboard';
        } catch (err) {
            errEl.textContent = err.message;
            btn.disabled = false;
            btn.textContent = 'Sign in';
        }
    });

    if (new URLSearchParams(location.search).get('expired')) {
        document.getElementById('login-error').textContent = 'Session expired. Please sign in again.';
    }
}

// ── Register page ──────────────────────────────────────────────

function initRegisterPage() {
    const form = document.getElementById('register-form');
    if (!form) return;

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const btn   = form.querySelector('[type=submit]');
        const errEl = document.getElementById('register-error');

        btn.disabled = true;
        errEl.textContent = '';

        try {
            await apiJson('/auth/register', {
                method: 'POST',
                body: JSON.stringify({
                    firstname: form.firstname.value.trim(),
                    lastname:  form.lastname.value.trim(),
                    email:     form.email.value.trim(),
                    password:  form.password.value,
                }),
            });
            showFlash('Registration successful. Check your email for the OTP.', 'success');
            window.location.href = '/ui/verify-otp';
        } catch (err) {
            errEl.textContent = err.businessErrorDescription || err.error || 'Registration failed';
            btn.disabled = false;
        }
    });
}

// ── OTP verify page ────────────────────────────────────────────

function initVerifyPage() {
    const form = document.getElementById('otp-form');
    if (!form) return;

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const btn   = form.querySelector('[type=submit]');
        const errEl = document.getElementById('otp-error');
        btn.disabled = true;
        errEl.textContent = '';

        try {
            await apiJson('/auth/verify-otp', {
                method: 'POST',
                body: JSON.stringify({ otp: form.otp.value.trim() }),
            });
            showFlash('Account verified!', 'success');
            window.location.href = '/ui/dashboard';
        } catch (err) {
            errEl.textContent = err.businessErrorDescription || err.error || 'Invalid OTP';
            btn.disabled = false;
        }
    });

    document.getElementById('resend-otp')?.addEventListener('click', async () => {
        try {
            await apiJson('/auth/send-otp', { method: 'POST' });
            showFlash('OTP resent to your email.', 'info');
        } catch {
            showFlash('Failed to resend OTP.', 'error');
        }
    });
}

// ── Weekly code page ───────────────────────────────────────────

function initWeeklyCodePage() {
    const form = document.getElementById('weekly-code-form');
    if (!form) return;

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const btn   = form.querySelector('[type=submit]');
        const errEl = document.getElementById('weekly-error');
        btn.disabled = true;
        errEl.textContent = '';

        try {
            await apiJson('/auth/validate-weekly-code', {
                method: 'POST',
                body: JSON.stringify({ code: form.code.value.trim() }),
            });
            showFlash('Weekly code accepted. Access granted.', 'success');
            window.location.href = '/ui/dashboard';
        } catch (err) {
            errEl.textContent = err.businessErrorDescription || err.error || 'Invalid code';
            btn.disabled = false;
        }
    });
}

// ── Dashboard ──────────────────────────────────────────────────

async function initDashboard() {
    if (!document.getElementById('dash-cameras')) return;

    try {
        const [cameras, alerts, zones] = await Promise.all([
            apiJson('/cameras'),
            apiJson('/alerts'),
            apiJson('/zones'),
        ]);

        document.getElementById('dash-cameras').textContent  = cameras?.total_elements ?? '—';
        document.getElementById('dash-alerts').textContent   = alerts?.total_elements  ?? '—';
        document.getElementById('dash-zones').textContent    = zones?.total_elements   ?? '—';
    } catch {
        // Silently degrade — numbers stay as '—'
    }

    // Recent alerts table
    try {
        const data = await apiJson('/alerts?page=0&size=5');
        renderAlertRows(data?.content ?? []);
    } catch { /* no alerts yet */ }
}

function renderAlertRows(alerts) {
    const tbody = document.getElementById('recent-alerts-body');
    if (!tbody) return;

    if (!alerts.length) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--text-hint);padding:24px">No recent alerts</td></tr>';
        return;
    }

    tbody.innerHTML = alerts.map(a => `
    <tr>
      <td><a href="/ui/alerts/${a.id}" class="text-accent">#${a.id}</a></td>
      <td>${escHtml(a.camera_name || '—')}</td>
      <td class="mono">${formatTime(a.timestamp)}</td>
      <td class="text-sm">${escHtml(a.description || '—')}</td>
    </tr>
  `).join('');
}

// ── Cameras ────────────────────────────────────────────────────

async function initCamerasPage() {
    if (!document.getElementById('cameras-tbody')) return;
    await loadCameras();
}

async function loadCameras(page = 0) {
    const tbody = document.getElementById('cameras-tbody');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-hint)">Loading…</td></tr>';

    try {
        const data = await apiJson(`/cameras?page_number=${page}&page_size=10`);
        const rows = data?.content ?? [];

        if (!rows.length) {
            tbody.innerHTML = '<tr><td colspan="6"><div class="empty-state"><div class="empty-icon">📷</div>No cameras configured</div></td></tr>';
            return;
        }

        tbody.innerHTML = rows.map(c => `
      <tr>
        <td class="mono">${c.id}</td>
        <td>${escHtml(c.name)}</td>
        <td class="mono">${escHtml(c.zone_name || '—')}</td>
        <td><span class="status-dot ${c.active ? 'green' : 'red'}"></span>${c.active ? 'Active' : 'Inactive'}</td>
        <td>
          <a href="/ui/cameras/${c.id}/feed" class="btn btn-sm btn-ghost">Feed</a>
          <a href="/ui/cameras/${c.id}/edit" class="btn btn-sm btn-ghost">Edit</a>
        </td>
      </tr>
    `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">Failed to load cameras: ${err.error || 'error'}</td></tr>`;
    }
}

async function initCameraForm() {
    const form = document.getElementById('camera-form');
    if (!form) return;

    // Populate zone dropdown
    try {
        const zones = await apiJson('/zones?page_number=0&page_size=100');
        const sel = document.getElementById('zone-select');
        (zones?.content ?? []).forEach(z => {
            const opt = document.createElement('option');
            opt.value = z.id;
            opt.textContent = z.name;
            sel.appendChild(opt);
        });
    } catch { /* no zones */ }

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const btn = form.querySelector('[type=submit]');
        btn.disabled = true;

        const gearChecks = [...form.querySelectorAll('[name="gear"]:checked')].map(c => c.value);
        const cameraId = form.dataset.cameraId;

        const payload = {
            name:                form.cameraName.value,
            ip_address:          form.ipAddress.value,
            port:                parseInt(form.port.value),
            username:            form.username.value,
            password:            form.password.value,
            rtsp_url:            form.rtspUrl.value,
            zone_id:             parseInt(form.querySelector('#zone-select').value),
            required_safety_gear: gearChecks,
            active:              form.querySelector('#active-toggle').checked,
            is_recording_active: form.querySelector('#recording-toggle').checked,
        };

        try {
            if (cameraId) {
                await apiJson(`/cameras/${cameraId}`, { method: 'PUT', body: JSON.stringify(payload) });
                showFlash('Camera updated.', 'success');
            } else {
                await apiJson('/cameras', { method: 'POST', body: JSON.stringify(payload) });
                showFlash('Camera added.', 'success');
                window.location.href = '/ui/cameras';
            }
        } catch (err) {
            showFlash(err.businessErrorDescription || 'Failed to save camera.', 'error');
        } finally {
            btn.disabled = false;
        }
    });
}

// ── Live Feed ──────────────────────────────────────────────────

function initCameraFeed() {
    const canvas = document.getElementById('feed-canvas');
    const cameraId = canvas?.dataset.cameraId;
    if (!canvas || !cameraId) return;

    const ctx = canvas.getContext('2d');
    const img = new Image();

    async function fetchFrame() {
        try {
            const res = await apiFetch(`/cameras/${cameraId}/feed`);
            if (!res || !res.ok) return;
            const blob = await res.blob();
            const url = URL.createObjectURL(blob);
            img.onload = () => {
                canvas.width  = img.naturalWidth  || canvas.offsetWidth;
                canvas.height = img.naturalHeight || canvas.offsetWidth * 9 / 16;
                ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                URL.revokeObjectURL(url);
            };
            img.src = url;
        } catch { /* connection lost */ }
    }

    fetchFrame();
    const interval = setInterval(fetchFrame, 100);

    // Clean up when navigating away
    window.addEventListener('beforeunload', () => clearInterval(interval));
}

// ── Zones ──────────────────────────────────────────────────────

async function initZonesPage() {
    if (!document.getElementById('zones-tbody')) return;

    const data = await apiJson('/zones?page_number=0&page_size=20').catch(() => null);
    const rows = data?.content ?? [];
    const tbody = document.getElementById('zones-tbody');

    if (!rows.length) {
        tbody.innerHTML = '<tr><td colspan="4"><div class="empty-state">No zones defined</div></td></tr>';
        return;
    }

    tbody.innerHTML = rows.map(z => `
    <tr>
      <td class="mono">${z.id}</td>
      <td>${escHtml(z.name)}</td>
      <td>${escHtml(z.description || '—')}</td>
      <td>${z.user_count ?? '—'}</td>
    </tr>
  `).join('');
}

async function initZoneForm() {
    const form = document.getElementById('zone-form');
    if (!form) return;

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const btn = form.querySelector('[type=submit]');
        btn.disabled = true;

        try {
            await apiJson('/zones', {
                method: 'POST',
                body: JSON.stringify({ name: form.zoneName.value, description: form.zoneDesc.value }),
            });
            showFlash('Zone created.', 'success');
            window.location.href = '/ui/zones';
        } catch (err) {
            showFlash(err.businessErrorDescription || 'Failed to create zone.', 'error');
            btn.disabled = false;
        }
    });
}

// ── Users ──────────────────────────────────────────────────────

async function initUsersPage() {
    if (!document.getElementById('users-tbody')) return;

    const data = await apiJson('/users?page_number=0&page_size=20').catch(() => null);
    const rows = data?.content ?? [];
    const tbody = document.getElementById('users-tbody');

    tbody.innerHTML = rows.map(u => `
    <tr>
      <td class="mono">${u.id}</td>
      <td>${escHtml(u.first_name)} ${escHtml(u.last_name)}</td>
      <td>
        <a href="/ui/users/${u.id}" class="btn btn-sm btn-ghost">View</a>
      </td>
    </tr>
  `).join('') || '<tr><td colspan="3"><div class="empty-state">No users found</div></td></tr>';
}

// ── Alerts ─────────────────────────────────────────────────────

async function initAlertsPage() {
    if (!document.getElementById('alerts-tbody')) return;

    const data = await apiJson('/alerts?page=0&size=20').catch(() => null);
    const rows = data?.content ?? [];
    const tbody = document.getElementById('alerts-tbody');

    tbody.innerHTML = rows.map(a => `
    <tr>
      <td class="mono"><a href="/ui/alerts/${a.id}" class="text-accent">#${a.id}</a></td>
      <td>${escHtml(a.camera_name || '—')}</td>
      <td class="mono">${formatTime(a.timestamp)}</td>
      <td>${escHtml(a.description || '—')}</td>
    </tr>
  `).join('') || '<tr><td colspan="4"><div class="empty-state"><div class="empty-icon">✅</div>No alerts — all clear</div></td></tr>';
}

// ── Profile ────────────────────────────────────────────────────

async function initProfilePage() {
    const section = document.getElementById('profile-section');
    if (!section) return;

    try {
        const user = await apiJson('/users/profile/me');
        document.getElementById('profile-name').textContent   = `${user.firstname} ${user.lastname}`;
        document.getElementById('profile-email').textContent  = user.email;
        document.getElementById('profile-active').textContent = user.active ? 'Active' : 'Inactive';
        document.getElementById('profile-otp').textContent    = user.otp_validated ? 'Yes' : 'No';
        document.getElementById('profile-weekly').textContent = user.weekly_code_validated ? 'Yes' : 'No';
    } catch { /* silent */ }
}

// ── Recordings ─────────────────────────────────────────────────

async function initRecordingsPage() {
    if (!document.getElementById('recordings-tbody')) return;

    const data = await apiJson('/recordings?page=0&size=20').catch(() => null);
    const rows = data?.content ?? [];
    const tbody = document.getElementById('recordings-tbody');

    tbody.innerHTML = rows.map(r => `
    <tr>
      <td class="mono">${r.id}</td>
      <td>${escHtml(r.camera_name || '—')}</td>
      <td class="mono">${formatTime(r.timestamp)}</td>
      <td>
        <a href="${API}/recordings/${r.id}/stream" class="btn btn-sm btn-ghost" target="_blank">Stream</a>
      </td>
    </tr>
  `).join('') || '<tr><td colspan="4"><div class="empty-state">No recordings found</div></td></tr>';
}

// ── Utils ──────────────────────────────────────────────────────

function escHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function formatTime(ts) {
    if (!ts) return '—';
    try {
        return new Date(ts).toLocaleString();
    } catch {
        return String(ts);
    }
}

// ── Bootstrap ──────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    // Logout button
    document.getElementById('logout-btn')?.addEventListener('click', e => {
        e.preventDefault();
        logout();
    });

    // Highlight active nav link
    const path = location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('href') && path.startsWith(link.getAttribute('href'))) {
            link.classList.add('active');
        }
    });

    // Init page-specific logic
    initLoginPage();
    initRegisterPage();
    initVerifyPage();
    initWeeklyCodePage();
    initDashboard();
    initCamerasPage();
    initCameraForm();
    initCameraFeed();
    initZonesPage();
    initZoneForm();
    initUsersPage();
    initAlertsPage();
    initProfilePage();
    initRecordingsPage();
});