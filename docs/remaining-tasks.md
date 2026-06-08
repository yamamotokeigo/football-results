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
git checkout main
git pull origin main
export EC2_PUBLIC_HOST=<current-ec2-public-ip>
export AWS_REGION=<aws-region>
export ECR_REGISTRY=<aws-account-id>.dkr.ecr.<aws-region>.amazonaws.com
export ECR_BACKEND_REPOSITORY=football-results-backend
export ECR_FRONTEND_REPOSITORY=football-results-frontend
export IMAGE_TAG=latest
export RDS_ENDPOINT=<rds-endpoint>
export RDS_DATABASE=football_results
export RDS_USERNAME=football
export RDS_PASSWORD=<rds-password>
docker compose -f docker-compose.yml -f docker-compose.ec2.yml down --remove-orphans
docker compose -f docker-compose.yml -f docker-compose.ec2.yml pull backend frontend
docker compose -f docker-compose.yml -f docker-compose.ec2.yml up -d
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
  - `EC2_SECURITY_GROUP_ID`
  - `AWS_ACCESS_KEY_ID`
  - `AWS_SECRET_ACCESS_KEY`
  - `AWS_ACCOUNT_ID`
  - `AWS_REGION`
  - `ECR_BACKEND_REPOSITORY`
  - `ECR_FRONTEND_REPOSITORY`
  - `RDS_ENDPOINT`
  - `RDS_DATABASE`
  - `RDS_USERNAME`
  - `RDS_PASSWORD`
- GitHub-hosted runners do not use the local client IP.
- The workflow temporarily allows SSH from the GitHub Actions runner IP, deploys over SSH, and removes that SSH rule afterward.
- The AWS credentials used by the workflow should be limited to security group ingress updates for the EC2 security group.

Minimum IAM actions for the current workflow:

```text
ec2:AuthorizeSecurityGroupIngress
ec2:RevokeSecurityGroupIngress
```

## CloudWatch Logs

- EC2 Docker Compose uses the `awslogs` logging driver in `docker-compose.ec2.yml`.
- Container logs are sent to the log group:

```text
/football-results/ec2
```

- Current log streams:
  - `frontend`
  - `backend`
  - `mysql`

The EC2 instance needs an IAM role that allows Docker to write logs to CloudWatch Logs. Minimum actions:

```text
logs:CreateLogGroup
logs:CreateLogStream
logs:DescribeLogStreams
logs:PutLogEvents
```

After deployment, logs can be checked from:

```text
AWS Console -> CloudWatch -> Logs -> Log groups -> /football-results/ec2
```

## ECR

- GitHub Actions builds frontend and backend images and pushes them to ECR.
- EC2 pulls the pushed images instead of building Docker images locally.
- Current ECR repository secrets:
  - `ECR_BACKEND_REPOSITORY`
  - `ECR_FRONTEND_REPOSITORY`

The GitHub Actions IAM user needs ECR push access. For the current learning setup, `AmazonEC2ContainerRegistryPowerUser` is acceptable.

The EC2 instance role needs ECR pull access:

```text
AmazonEC2ContainerRegistryReadOnly
```

## RDS MySQL

- EC2 deployment uses RDS MySQL instead of the local MySQL container.
- `docker-compose.ec2.yml` disables the MySQL service by putting it behind the `local-mysql` profile.
- Backend datasource settings are injected from GitHub Secrets during deployment.
- RDS should not be publicly accessible.
- RDS Security Group should allow MySQL/Aurora port `3306` only from the EC2 Security Group.

Required repository secrets:

```text
RDS_ENDPOINT
RDS_DATABASE
RDS_USERNAME
RDS_PASSWORD
```

## Cost Control

- Confirm the EC2 instance is stopped when finished.
- Confirm no Elastic IP is allocated unless it is intentionally used.
- Confirm no RDS, ALB, ECS service, or unused snapshot has been created unintentionally.
- Public IPv4, EBS volume storage, and retained snapshots can still incur cost.

## Next Improvements

1. Introduce ALB and HTTPS.
2. Move from Docker Compose on EC2 to ECS.
3. Split dev and production environments after the low-cost deployment flow is stable.
