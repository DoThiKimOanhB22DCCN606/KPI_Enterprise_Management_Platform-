// Auth-service signs with: jwtSecret.getBytes()  (NOT base64-decoded)
// jwt.secret in docker env is: YmFzZTY0LWVuY29kZWQtc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmc=
// So the HMAC key = that base64 string's UTF-8 bytes

const jwtSecret = "YmFzZTY0LWVuY29kZWQtc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmc=";
const secretBytes = Buffer.from(jwtSecret, 'utf8');  // <-- raw UTF-8 bytes of the string

function base64url(obj) {
  return Buffer.from(JSON.stringify(obj)).toString('base64url');
}

const crypto = require('crypto');
const header = base64url({ alg: 'HS256', typ: 'JWT' });
const now = Math.floor(Date.now() / 1000);
const payload = base64url({
  sub: "00000000-0000-0000-0000-000000000002",
  tenantId: "00000000-0000-0000-0000-000000000001",
  roles: ["USER"],
  iat: now,
  exp: now + 3600
});

const signingInput = `${header}.${payload}`;
const signature = crypto.createHmac('sha256', secretBytes).update(signingInput).digest('base64url');
const token = `${signingInput}.${signature}`;
console.log("TOKEN:", token);
console.log("");

// Test against ai-service directly
async function testDirect(jwt, endpoint, method = 'GET', body = null) {
  const opts = {
    method,
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Content-Type': 'application/json'
    }
  };
  if (body) opts.body = JSON.stringify(body);
  
  try {
    const res = await fetch(endpoint, opts);
    console.log(`[${method}] ${endpoint} → ${res.status}`);
    const text = await res.text();
    if (text) console.log("Body:", text.substring(0, 500));
  } catch (err) {
    console.error(`[${method}] ${endpoint} → NETWORK ERROR:`, err.message);
  }
}

async function run() {
  // 1. Test GET first (should work since we know it returns 200)
  await testDirect(token, 'http://localhost:8089/v1/ai/conversations');
  
  // 2. Test POST query - THIS is the problematic one
  console.log("\nTesting POST /v1/ai/query (may hang if OpenAI key missing)...");
  const controller = new AbortController();
  const timeout = setTimeout(() => { controller.abort(); }, 5000);
  
  try {
    const res = await fetch('http://localhost:8089/v1/ai/query', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ prompt: "test" }),
      signal: controller.signal
    });
    clearTimeout(timeout);
    console.log(`POST /v1/ai/query → ${res.status}`);
    const text = await res.text();
    console.log("Body:", text.substring(0, 500));
  } catch (err) {
    clearTimeout(timeout);
    if (err.name === 'AbortError') {
      console.log("POST /v1/ai/query → TIMED OUT after 5s (request is hanging!)");
    } else {
      console.error("POST /v1/ai/query → ERROR:", err.message);
    }
  }
}

run();
