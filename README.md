# Hueno website

Static landing page and privacy policy for the Hueno Android app. This branch (`website`) has no
shared history with `main`; the app source lives there.

| Path | Page |
|---|---|
| `/` | `index.html`, landing page |
| `/privacy` | `privacy.html`, privacy policy (link this URL in Play Console) |
| any other | `404.html` |

No build step, no JavaScript, no third-party requests. Screenshots in `assets/` are real captures
from the app on a Galaxy Z Flip4 (see `store-assets/` on `main`).

## Deploy on Cloudflare Pages

1. Cloudflare dashboard → Workers & Pages → Create → Pages → Connect to Git.
2. Pick the repository `baluadev/Hueno-app`.
3. Production branch: `website`.
4. Framework preset: None. Build command: leave empty. Build output directory: `/`.
5. Save and Deploy. The site is served at `https://<project>.pages.dev`; add a custom domain under
   the project's Custom domains tab if you have one.

Cloudflare Pages serves `privacy.html` at `/privacy` and uses `404.html` for unknown paths.
`_headers` adds security headers.

## Keep the policy true

The privacy policy describes what the app actually does. Update it, and its date, whenever the app
adds an SDK, a permission, analytics, crash reporting, or anything that sends data off the phone.
