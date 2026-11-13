# বাংলার AI — Shared AI Backend

The Android app calls this Worker. The Groq API key is stored as a Cloudflare Worker secret and is never included in the APK or shown in app Settings.

## Deploy (one-time owner setup)

1. Create/sign in to a Cloudflare account.
2. Install Wrangler on a computer, then run from this folder:
   `npx wrangler login`
3. Add the provider key as a secret (do not paste it into GitHub or chat):
   `npx wrangler secret put GROQ_API_KEY`
4. Deploy:
   `npx wrangler deploy`
5. Copy the deployed `https://<worker-name>.<account-subdomain>.workers.dev` URL.
6. Set that URL in `AI_BACKEND_URL` in `app/src/main/java/com/banglarai/app/MainActivity.kt`, keeping the `/v1/chat/completions` path. Then build the public APK.

## Required before public launch

- Add a Cloudflare WAF rate-limiting rule for this Worker route (for example, limit repeated POST requests per source IP). Tune it to real usage and provider limits.
- Configure billing/spend limits and usage alerts with the AI provider. The provider key is shared by all users, so uncontrolled use can create costs.
- Monitor request volume and errors. Add stronger abuse prevention and per-installation/user quotas before a wide public release.
- Never commit the provider key, expose it in client code, or ask users to enter it.
- The current Worker validates request shape and size, but it does not authenticate individual app installations. Rate limiting at the edge is essential; a public endpoint can otherwise be abused.
- Do not treat a successful Android build as proof that the Worker is deployed or the live AI service works. Test the deployed endpoint separately.

## API

`POST /v1/chat/completions`

Request body:
```json
{ "messages": [{ "role": "user", "content": "হ্যালো" }] }
```

The Worker fixes the model and token limit server-side; the app cannot select a different provider model.
