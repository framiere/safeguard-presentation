#!/bin/bash

# Step 1: Build the plugin
mvn clean package

# Step 2: Configure the gateway
cp target/plugin.jar path/to/your/plugin.jar

# Step 3: Run Kafka and Conduktor Gateway
docker-compose up -d

# Step 4: Wait for services to be up and running
sleep 10

# Step 5: Execute topic creation with 150 partitions and replication factor 2
docker-compose exec kafka \
kafka-topics \
--bootstrap-server localhost:9092 \
--topic cars \
--replication-factor 2
--partitions 150 \
--create

# Step 6: Describe the created topic to verify the partitions and replication factor
docker-compose exec kafka \
kafka-topics \
--bootstrap-server localhost:9092 \
--topic cars \
--describe

# Step 7: Execute topic creation with 10 partitions and replication factor 1
docker-compose exec kafka \
kafka-topics \
--bootstrap-server localhost:9092 \
--topic cars \
--replication-factor 1 \
--partitions 10 \
--create

# Step 8: Describe the created topic to verify the partitions and replication factor
docker-compose exec kafka \
kafka-topics \
--bootstrap-server localhost:9092 \
--topic cars \
--describe

# Step 9: Execute topic creation with 150 partitions and replication factor 200
docker-compose exec kafka \
kafka-topics \
--bootstrap-server localhost:9092 \
--topic cars \
--replication-factor 200 \
--partitions 150 \
--create

# Step 10: Describe the created topic to verify the partitions and replication factor
docker-compose exec kafka \
kafka-topics \
--bootstrap-server localhost:9092 \
--topic cars \
--describe
