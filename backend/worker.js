const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
const MODEL = "openai/gpt-oss-120b";
const MAX_BODY_BYTES = 24 * 1024;
const MAX_MESSAGES = 20;
const MAX_MESSAGE_CHARS = 6000;

function json(data, status = 200, origin = "*") {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-store",
      "Access-Control-Allow-Origin": origin,
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
    }
  });
}

export default {
  async fetch(request, env) {
    const origin = request.headers.get("Origin") || "*";
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": origin,
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type",
          "Access-Control-Max-Age": "86400"
        }
      });
    }

    const url = new URL(request.url);
    if (url.pathname !== "/v1/chat/completions" || request.method !== "POST") {
      return json({ error: "Not found" }, 404, origin);
    }
    if (!env.GROQ_API_KEY) {
      return json({ error: "AI service is not configured" }, 503, origin);
    }

    const declaredLength = Number(request.headers.get("Content-Length") || "0");
    if (declaredLength > MAX_BODY_BYTES) {
      return json({ error: "Request too large" }, 413, origin);
    }

    let payload;
    try {
      const raw = await request.text();
      if (new TextEncoder().encode(raw).length > MAX_BODY_BYTES) {
        return json({ error: "Request too large" }, 413, origin);
      }
      payload = JSON.parse(raw);
    } catch {
      return json({ error: "Invalid JSON" }, 400, origin);
    }

    if (!Array.isArray(payload.messages) ||
        payload.messages.length < 1 ||
        payload.messages.length > MAX_MESSAGES) {
      return json({ error: "Invalid message list" }, 400, origin);
    }

    const allowedRoles = new Set(["system", "user", "assistant"]);
    const messages = [];
    for (const item of payload.messages) {
      if (!item || !allowedRoles.has(item.role) ||
          typeof item.content !== "string" ||
          item.content.length < 1 ||
          item.content.length > MAX_MESSAGE_CHARS) {
        return json({ error: "Invalid message" }, 400, origin);
      }
      messages.push({ role: item.role, content: item.content });
    }

    try {
      const upstream = await fetch(GROQ_URL, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${env.GROQ_API_KEY}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: MODEL,
          messages,
          max_completion_tokens: 1200,
          temperature: 0.7
        })
      });

      const text = await upstream.text();
      return new Response(text, {
        status: upstream.status,
        headers: {
          "Content-Type": "application/json; charset=utf-8",
          "Cache-Control": "no-store",
          "Access-Control-Allow-Origin": origin
        }
      });
    } catch {
      return json({ error: "Upstream AI service unavailable" }, 502, origin);
    }
  }
};
