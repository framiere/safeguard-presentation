package fr.devoxx.demo.safeguard;

import io.conduktor.gateway.interceptor.DirectionType;
import io.conduktor.gateway.interceptor.Interceptor;
import io.conduktor.gateway.interceptor.InterceptorContext;
import org.apache.commons.collections4.IterableUtils;
import org.apache.kafka.common.record.*;
import org.apache.kafka.common.requests.ProduceRequest;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ChaosProducerInterceptor implements Interceptor<ProduceRequest> {
    @Override
    public CompletionStage<ProduceRequest> intercept(ProduceRequest request, InterceptorContext interceptorContext) {
        if (interceptorContext.direction() == DirectionType.REQUEST) {
            request.data().topicData().stream()
                    .filter(e -> e.name().contains("devoxx"))
                    .forEach(this::writeYoloDevoxx);
        }
        return CompletableFuture.completedStage(request);
    }

    private void writeYoloDevoxx(ProduceRequest.TopicData topicData) {
        for (ProduceRequest.PartitionData partition : topicData.partitions()) {
            MemoryRecords records = (MemoryRecords) partition.records();

            int bufSize = IterableUtils.toList(records.batches())
                    .stream()
                    .mapToInt(RecordBatch::sizeInBytes)
                    .sum();
            ByteBuffer buf = ByteBuffer.allocate(bufSize * 4);
            for (RecordBatch batch : records.batches()) {
                writeRecords(buf, batch);
            }
            buf.flip();
            MemoryRecords newRecords = MemoryRecords.readableRecords(buf);
            partition.setRecords(newRecords);
        }
    }

    private void writeRecords(ByteBuffer buf, RecordBatch batch) {
        Iterable<Record> records = IterableUtils.toList(batch);
        long baseOffset = IterableUtils.toList(records)
                .stream()
                .mapToLong(Record::offset)
                .min()
                .orElse(0L);

        try (MemoryRecordsBuilder builder = MemoryRecords.builder(
                buf,
                batch.magic(),
                batch.compressionType(),
                TimestampType.CREATE_TIME,
                baseOffset,
                RecordBatch.NO_TIMESTAMP,
                batch.producerId(),
                batch.producerEpoch(),
                batch.baseSequence(),
                batch.isTransactional(),
                batch.isControlBatch(),
                batch.partitionLeaderEpoch()
        )) {
            for (Record record : records) {
                builder.append(
                        new SimpleRecord(
                                record.timestamp(),
                                record.key(),
                                ByteBuffer.allocate(record.value().remaining() + 100)
                                        .put("yolo ".getBytes())
                                        .put(record.value())
                                        .put((" at " + LocalDateTime.now()).getBytes())
                                        .rewind(),
                                record.headers()
                        )
                );
            }
            builder.build();
        }
    }
}
