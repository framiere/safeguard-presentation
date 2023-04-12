

```
docker compose up -d

echo "hello" | kafka-console-producer.sh --topic test --bootstrap-server localhost:7070 --producer-property 'acks=1'
kafka-console-producer.sh --topic test --bootstrap-server localhost:7070 --producer-property 'acks=-1'

kafka-console-consumer.sh --bootstrap-server localhost:7070 --topic test
```