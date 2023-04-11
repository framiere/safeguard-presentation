package fr.devoxx.demo.safeguard;

import io.conduktor.gateway.interceptor.DirectionType;
import io.conduktor.gateway.interceptor.Interceptor;
import io.conduktor.gateway.interceptor.InterceptorContext;
import org.apache.commons.collections4.IterableUtils;
import org.apache.kafka.common.message.ProduceRequestData;
import org.apache.kafka.common.record.Record;
import org.apache.kafka.common.record.*;
import org.apache.kafka.common.requests.ProduceRequest;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.time.LocalDateTime.now;

public class ProduceInterceptor implements Interceptor<ProduceRequest> {
    @Override
    public CompletionStage<ProduceRequest> intercept(ProduceRequest request, InterceptorContext interceptorContext) {
        if (interceptorContext.direction() == DirectionType.REQUEST) {
            request.data().topicData().stream()
                    .filter(e -> e.name().contains("devoxx"))
                    .forEach(this::writeYoloDevoxx);
        }
        return CompletableFuture.completedStage(request);
    }

    private void writeYoloDevoxx(ProduceRequestData.TopicProduceData topicData) {
        for (var partition : topicData.partitionData()) {
            var records = (MemoryRecords) partition.records();

            var bufSize = IterableUtils.toList(records.batches())
                    .stream()
                    .mapToInt(RecordBatch::sizeInBytes)
                    .sum();
            var buf = ByteBuffer.allocate(bufSize * 4);
            for (var batch : records.batches()) {
                writeRecords(buf, batch);
            }
            buf.flip();
            var newRecords = MemoryRecords.readableRecords(buf);
            partition.setRecords(newRecords);
        }
    }

    private void writeRecords(ByteBuffer buf, RecordBatch batch) {
        var records = IterableUtils.toList(batch);
        long baseOffset = records.stream()
                .map(Record::offset)
                .min(Comparator.comparingLong(e -> e)).orElse(0L);

        try (MemoryRecordsBuilder builder = new MemoryRecordsBuilder(
                buf,
                batch.magic(),
                batch.compressionType(),
                batch.timestampType(),
                baseOffset,
                RecordBatch.NO_TIMESTAMP,
                batch.producerId(),
                batch.producerEpoch(),
                batch.baseSequence(),
                batch.isTransactional(),
                batch.isControlBatch(),
                batch.partitionLeaderEpoch(),
                buf.capacity()
        )) {
            records.forEach(record -> {
                // Here!
                builder.append(new SimpleRecord(
                        record.timestamp(),
                        record.key(),
                        ByteBuffer.allocate(record.valueSize() + 100)
                                .put("yolo ".getBytes())
                                .put(record.value())
                                .put((" at " + now()).getBytes())
                                .rewind(),
                        record.headers()
                ));
            });
            builder.build();
        }
    }
}
