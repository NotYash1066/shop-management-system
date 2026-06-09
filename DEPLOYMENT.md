# Shop Management - Deployment Guide

This document explains how to deploy **Shop Management System** (Spring Boot backend + Next.js frontend) to production with minimal hassle and good security.

## Architecture Overview

- **Backend**: Spring Boot 3.4 + Java 17 → PostgreSQL + Redis
- **Frontend**: Next.js 15 (App Router) → calls backend via `NEXT_PUBLIC_API_BASE_URL`
- Recommended combo: **Railway (backend + DBs) + Vercel (frontend)**

---

## 1. Quickest Path (Recommended)

### Backend (Railway)

1. Push this repo to GitHub (if not already).
2. Go to [Railway.app](https://railway.app) → New Project → Deploy from GitHub repo.
3. Select the `shop-management-system` folder as the root (or use a monorepo setup and set the root directory in Railway service settings).
4. Railway will detect the `Dockerfile` (or `nixpacks.toml`) and build automatically.
5. Add two services:
   - **PostgreSQL** (official plugin)
   - **Redis** (official plugin)
6. In the backend service, set these **environment variables** (from the dashboard):

   ```
   SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}   # Railway will inject the real URL
   SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
   SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}

   REDIS_URL=${{Redis.REDIS_URL}}

   APP_JWT_SECRET=<generate with: openssl rand -base64 64>
   ```

7. (Optional but recommended) Set:
   ```
   CORS_ALLOWED_ORIGINS=https://your-frontend-domain.vercel.app
   ```

8. Deploy. The health check is already configured on `/api/test`.

### Frontend (Vercel)

1. In Vercel, import the repo and select the `shop-management-frontend` folder as the root.
2. Add environment variable:
   ```
   NEXT_PUBLIC_API_BASE_URL=https://your-railway-backend.up.railway.app/api
   ```
3. Deploy.

That's it. Every push to `main` will trigger new builds via the GitHub Actions we created.

---

## 2. Environment Variables Reference

See `.env.example` in `shop-management-system/` for the full list with comments.

**Must-set in production**:
- Database connection (via Railway variables or direct)
- `REDIS_URL`
- `APP_JWT_SECRET` (strong random value)

**Frontend only**:
- `NEXT_PUBLIC_API_BASE_URL`

---

## 3. What the GitHub Actions Do

Located in `.github/workflows/`:

- `ci.yml` (backend): Runs on pushes/PRs to `shop-management-system/`
  - Starts Postgres + Redis
  - Runs `./mvnw test`
  - Builds the JAR
  - Builds the Docker image (to verify it works from a clean checkout)

- `ci.yml` (frontend): Runs on pushes/PRs to `shop-management-frontend/`
  - `npm ci`
  - `npm run lint`
  - `npm run build`

These give you confidence before merging/deploying.

---

## 4. Docker Notes

The `Dockerfile` is now a proper **multi-stage build**:
- Stage 1: Uses `eclipse-temurin:17-jdk-alpine` to run `./mvnw clean package -DskipTests`
- Stage 2: Uses `eclipse-temurin:17-jre-alpine` + only copies the final JAR

This means you can deploy from a fresh `git clone` on Railway, Render, Fly, etc. without needing to build the JAR locally first.

`.dockerignore` is also hardened to keep the image small and avoid leaking secrets.

---

## 5. Alternative Platforms

- **Render.com**: Works fine. Use the Dockerfile or their native Java builder. Add Postgres + Redis as separate services.
- **Fly.io**: Good if you want global edge. Use the Dockerfile.
- **Self-hosted / VPS**: Use the Dockerfile + Docker Compose (you'll need to manage Postgres/Redis yourself).

---

## 6. Security Checklist (Do These)

- [ ] `APP_JWT_SECRET` is long and random (use `openssl rand -base64 64`)
- [ ] `CORS_ALLOWED_ORIGINS` is set to your actual frontend domain(s) in production (not `*`)
- [ ] Never commit real `.env` files (we've hardened `.gitignore`)
- [ ] Use Railway/Render injected database URLs instead of hardcoding
- [ ] Review PRs before merging to `main` (GitHub Actions will run on PRs)

---

## 7. Local Development vs Production

For local:
```bash
cd shop-management-system
# Set up local Postgres + Redis, then
SPRING_DATASOURCE_URL=... REDIS_URL=... ./mvnw spring-boot:run
```

For frontend:
```bash
cd shop-management-frontend
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api npm run dev
```

---

## 8. Troubleshooting

- **Health check failing**: Make sure `/api/test` returns 200. Check logs in Railway.
- **CORS errors in browser**: `CORS_ALLOWED_ORIGINS` on backend must include your Vercel URL.
- **Database connection refused**: Double-check that the Postgres service is attached to the same Railway project and the variable references are correct (`${{Postgres.DATABASE_URL}}` etc.).
- **JWT validation failing**: Regenerate `APP_JWT_SECRET` and redeploy.

---

## 9. Next Steps After First Deploy

- Set up a custom domain on Vercel (frontend) and point backend CORS to it.
- Add monitoring (Railway has basic logs; you can add Sentry or similar later).
- Consider adding a staging environment (different Railway project or branch deploys).

---

**You now have production-grade Docker, hardened ignores, CI that actually tests, and clear deployment instructions.**

The only things that require your manual action are:
1. Creating the Railway project + adding Postgres/Redis.
2. Creating the Vercel project.
3. Pasting the generated secrets into the dashboards.

Everything else (code, Docker, workflows, docs) has been prepared automatically.
