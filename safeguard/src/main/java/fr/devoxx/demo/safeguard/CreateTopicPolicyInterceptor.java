package fr.devoxx.demo.safeguard;

import io.conduktor.gateway.interceptor.Interceptor;
import io.conduktor.gateway.interceptor.InterceptorContext;
import io.conduktor.gateway.rebuilder.exception.GatewayIntentionException;
import org.apache.kafka.common.errors.PolicyViolationException;
import org.apache.kafka.common.requests.CreateTopicsRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CreateTopicPolicyInterceptor implements Interceptor<CreateTopicsRequest> {
    @Override
    public CompletionStage<CreateTopicsRequest> intercept(CreateTopicsRequest request, InterceptorContext context) {
        if (request.data().topics().stream().anyMatch(e -> e.numPartitions() > 10)) {
            return CompletableFuture.failedFuture(
                    new GatewayIntentionException("Topic with too much partitions",
                            request.getErrorResponse(
                                    new PolicyViolationException("Safeguard told me you should reconsider your partitions"))));
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
