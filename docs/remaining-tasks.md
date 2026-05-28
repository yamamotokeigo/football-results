# Remaining Tasks

## Current Status

- Local Docker setup has been migrated from H2 to MySQL.
- EC2 deployment using Docker Compose has been verified.
- The app can be accessed through the EC2 public IPv4 address.
- GitHub Actions workflow for SSH deployment has been added.

## Local Application

- Keep using `docker compose up -d --build` for local verification.
- MySQL data is persisted in the `mysql-data` Docker volume.
- Do not run `docker compose down -v` unless the local database can be deleted.

## EC2 Operation

- Stop the EC2 instance when not using it to reduce cost.
- After starting the instance again, confirm the new public IPv4 address.
- If the public IPv4 address changes, update the GitHub secret `EC2_HOST`.
- Before deploying manually, SSH into the instance and run:

```bash
cd ~/football-results
git fetch origin
git checkout feature/ec2-http-deploy
git pull origin feature/ec2-http-deploy
export EC2_PUBLIC_HOST=<current-ec2-public-ip>
docker compose -f docker-compose.yml -f docker-compose.ec2.yml down
docker compose -f docker-compose.yml -f docker-compose.ec2.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.ec2.yml ps
```

## Security Group

- Keep SSH port `22` restricted to the current client IP during normal operation.
- Open HTTP port `80` only while the app needs to be publicly accessible.
- Do not expose MySQL port `3306` publicly.
- Do not expose backend port `8081` publicly unless debugging requires it.

## GitHub Actions Deployment

- Current workflow: `.github/workflows/deploy-ec2.yml`.
- EC2 uses `docker-compose.ec2.yml` to publish frontend on port `80` while keeping backend and MySQL off the public network.
- Required repository secrets:
  - `EC2_HOST`
  - `EC2_USER`
  - `EC2_SSH_KEY`
  - `EC2_APP_DIR`
- GitHub-hosted runners do not use the local client IP.
- For the current SSH deployment approach, temporarily allow SSH from `0.0.0.0/0` before running the workflow, then restore it to the client IP after deployment.

## Cost Control

- Confirm the EC2 instance is stopped when finished.
- Confirm no Elastic IP is allocated unless it is intentionally used.
- Confirm no RDS, ALB, ECS service, or unused snapshot has been created unintentionally.
- Public IPv4, EBS volume storage, and retained snapshots can still incur cost.

## Next Improvements

1. Add a safer GitHub Actions deployment flow that temporarily opens SSH only for the runner IP.
2. Add CloudWatch Logs or another simple log collection path.
3. Move Docker image builds to ECR.
4. Move MySQL from the local container to RDS MySQL.
5. Introduce ALB and HTTPS.
6. Move from Docker Compose on EC2 to ECS.
7. Split dev and production environments after the low-cost deployment flow is stable.
