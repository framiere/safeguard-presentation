Notes:

(1) The "docker-compose.yml" file includes the necessary services for Kafka, Conduktor Gateway, and any other required components.
Replace "your-gateway-image" with the appropriate image for your Conduktor Gateway instance. Make sure to mount the plugin JAR file inside the container at the appropriate location.

(2) In accordance with the second step (task). The plugin code ("CreateTopicPolicyInterceptor.java") was modified to enforce partition and replication factor limits as specified.

In the updated script:
- The MAX_PARTITIONS constant represents the maximum allowed number of partitions (10 in this case).
- The MIN_REPLICATION_FACTOR constant represents the minimum allowed replication factor (2 in this case).
- The DEFAULT_PARTITIONS constant represents the default number of partitions to be set if the input exceeds the maximum allowed value.
- The DEFAULT_REPLICATION_FACTOR constant represents the default replication factor to be set if the input is below the minimum allowed value.
- Within the intercept method, the script iterates over the topics in the CreateTopicsRequest and enforces the limits on the number of partitions and replication factor. If the number of partitions exceeds the maximum, it is forced to the default value. If the replication factor is below the minimum, it is set to the default value.

The script still checks if the topic name starts with "devoxx" and throws an exception if it doesn't meet the policy. You can modify this behavior or remove it according to your requirements.

Please note that the code provided assumes that you have the necessary dependencies and configurations in place to compile and use the modified script within your Conduktor Gateway environment.

(3) Script Execution to automate the execution of the required commands ("run.sh"):
Make sure to replace "path/to/your/plugin.jar" with the actual path to your built plugin JAR file.

To run the script, make it executable by running chmod +x run.sh, and then execute it using ./run.sh in your terminal.

The script performs the following steps:
- Builds the plugin using Maven.
- Copies the built plugin JAR file to the appropriate location.
- Starts Kafka and Conduktor Gateway using Docker Compose.
- Waits for the services to be up and running.
- Executes the Kafka topic creation command with 150 partitions and replication factor 2 for the "cars" topic.
- Describes the created topic to verify that it has 10 partitions and a replication factor of 2.
- Executes the Kafka topic creation command with 10 partitions and replication factor 1 for the "cars" topic.
- Describes the created topic to verify that it still has 10 partitions and a replication factor of 2.
- Executes the Kafka topic creation command with 150 partitions and replication factor 200 for the "cars" topic.
- Describes the created topic to verify that it has been forced to 10 partitions and a replication factor of 2.
- Please note that you may need to adjust the commands or configuration based on your specific environment and requirements.

I hope this helps you validate the behavior of the plugin and the topic creation process as expected!
