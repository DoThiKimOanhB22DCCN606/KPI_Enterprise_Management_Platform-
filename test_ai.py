import jwt
import base64
import time
import requests
import json
import uuid

secret = base64.b64decode("YmFzZTY0LWVuY29kZWQtc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmc=")
payload = {
    "sub": "00000000-0000-0000-0000-000000000002",
    "tenantId": "00000000-0000-0000-0000-000000000001",
    "iat": int(time.time()),
    "exp": int(time.time()) + 3600
}
token = jwt.encode(payload, secret, algorithm="HS256")

url = "http://localhost:8089/v1/ai/query"
headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json",
    "X-Tenant-Id": "00000000-0000-0000-0000-000000000001",
    "X-User-Id": "00000000-0000-0000-0000-000000000002"
}
data = {
    "prompt": "Compare the customer satisfaction (CSAT) trends between Store 1 and Store 2 over the last 3 months.",
    "conversationId": str(uuid.uuid4())
}

print(f"Sending request to {url}")
response = requests.post(url, headers=headers, json=data)
print(f"Status: {response.status_code}")
print(f"Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
