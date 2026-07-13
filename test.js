async function test() {
  try {
    const res = await fetch('http://localhost:8089/v1/ai/query', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer test',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ prompt: "test" })
    });
    
    console.log("Status:", res.status);
    const text = await res.text();
    console.log("Data:", text);
  } catch (err) {
    console.error("Fetch error:", err.message);
  }
}

test();
