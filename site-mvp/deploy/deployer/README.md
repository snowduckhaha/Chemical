# Deployment agent

The backend never runs shell commands. It only creates a `deployment_job`; this single-process agent polls jobs for one fixed environment and executes the fixed script in this directory.

Install it on the local machine or production server outside Docker, using a dedicated checkout and a dedicated operating-system account. It needs Docker access for the configured environment.

```bash
export DEPLOY_APP_DIR=/opt/chemical/app/site-mvp
export DEPLOY_ENVIRONMENT=PRODUCTION  # or LOCAL
export DEPLOY_LOG_DIR=/opt/chemical/deployer-logs
bash /opt/chemical/app/site-mvp/deploy/deployer/deployment-agent.sh
```

For production, the agent loads database settings from `$DEPLOY_APP_DIR/.env`. Keep `DEPLOY_APP_DIR` as the deployed application directory and start one agent only. A `PRODUCTION` job runs the fixed `deploy/scripts/deploy.sh` on this host: it fast-forwards `main`, builds the backend, builds Vite-SSG against `http://backend:8080/api/v1` on the private Docker network, and then recreates the services. The CMS endpoint is restricted to the `zelin` account with the `ADMIN` role; it only creates `PRODUCTION` jobs for `origin/main`. No GitHub Actions runner or GHCR write token is involved. A `LOCAL` job uses the fixed local start/stop scripts.

Before the first deployment task is created on an existing server, pull this release and apply the additive job-table migration once:

```bash
docker compose --env-file .env -f deploy/docker-compose.prod.yml up -d mysql
docker compose --env-file .env -f deploy/docker-compose.prod.yml exec -T mysql \
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" qidian_site < sql/V19__deployment_jobs.sql
```

Do not expose the agent as an HTTP endpoint and do not add request-provided commands, branches, paths, or environment variables.
