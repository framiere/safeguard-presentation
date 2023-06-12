package fr.devoxx.demo.safeguard;

import io.conduktor.gateway.interceptor.Interceptor;
import io.conduktor.gateway.interceptor.InterceptorContext;
import io.conduktor.gateway.rebuilder.exception.GatewayIntentionException;
import org.apache.kafka.common.errors.PolicyViolationException;
import org.apache.kafka.common.requests.CreateTopicsRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CreateTopicPolicyInterceptor implements Interceptor<CreateTopicsRequest> {

    private static final int MAX_PARTITIONS = 10;
    private static final int MIN_REPLICATION_FACTOR = 2;
    private static final int DEFAULT_PARTITIONS = 10;
    private static final int DEFAULT_REPLICATION_FACTOR = 2;

    @Override
    public CompletionStage<CreateTopicsRequest> intercept(CreateTopicsRequest request, InterceptorContext context) {
        for (CreateTopicsRequest.TopicDetails topic : request.data().topics()) {
            if (topic.numPartitions() > MAX_PARTITIONS) {
                topic.numPartitions(DEFAULT_PARTITIONS);
            }
            if (topic.replicationFactor() < MIN_REPLICATION_FACTOR) {
                topic.replicationFactor(DEFAULT_REPLICATION_FACTOR);
            }
        }

        if (request.data().topics().stream().noneMatch(e -> e.name().startsWith("devoxx"))) {
            return CompletableFuture.failedFuture(
                    new GatewayIntentionException("Topic name is not within policy",
                            request.getErrorResponse(
                                    new PolicyViolationException("Safeguard told me your topic name is not cool enough"))));
        }
        return CompletableFuture.completedStage(request);
    }
}
