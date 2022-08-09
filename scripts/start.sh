#!/usr/bin/env bash

ABS_PATH=$(readlink -f "$0")
ABS_DIR=$(dirname "$ABS_PATH")
source "${ABS_DIR}"/profile.sh

IDLE_PORT=$(find_idle_port)
REPOSITORY=/home/ec2-user/deploy

echo "> Deploy new application"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> Jar name: $JAR_NAME"

echo "> Grant execution permission to $JAR_NAME"

chmod +x "$JAR_NAME"

echo "> Execute $JAR_NAME"

IDLE_PROFILE=$(find_idle_profile)

echo "> Execute $JAR_NAME in profile=$IDLE_PROFILE"

cd $REPOSITORY || exit

DB_URL=$(aws ssm get-parameters --region ap-northeast-2 --names DB_URL --query Parameters[0].Value | sed 's/"//g')
DB_USERNAME=$(aws ssm get-parameters --region ap-northeast-2 --names DB_USERNAME --query Parameters[0].Value | sed 's/"//g')
DB_PASSWORD=$(aws ssm get-parameters --region ap-northeast-2 --names DB_PASSWORD --query Parameters[0].Value | sed 's/"//g')
ADMIN_PASSWORD=$(aws ssm get-parameters --region ap-northeast-2 --names ADMIN_PASSWORD --query Parameters[0].Value | sed 's/"//g')

sudo docker build -t spring ./
sudo docker run -it --name "$IDLE_PROFILE" -d \
-e active="$IDLE_PROFILE" \
-e DB_URL="$DB_URL" \
-e DB_USERNAME="$DB_USERNAME" \
-e DB_PASSWORD="$DB_PASSWORD" \
-e ADMIN_PASSWORD="$ADMIN_PASSWORD" \
-p "$IDLE_PORT":"$IDLE_PORT" spring
