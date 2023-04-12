package fr.devoxx.demo.safeguard;

import io.conduktor.gateway.interceptor.Interceptor;
import io.conduktor.gateway.interceptor.Plugin;

import java.util.List;
import java.util.Map;

public class DevoxxSafeguardPlugin implements Plugin {

    @Override
    public void configure(Map<String, Object> map) {

    }

    @Override
    public List<Interceptor> getInterceptors() {
        return List.of(
                new AckAllPolicyInterceptor(),
                new ChaosProducerInterceptor(),
                new CreateTopicPolicyInterceptor()
        );
    }
}
