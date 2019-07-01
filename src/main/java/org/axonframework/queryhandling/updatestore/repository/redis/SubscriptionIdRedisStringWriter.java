package org.axonframework.queryhandling.updatestore.repository.redis;

import org.axonframework.queryhandling.SubscriptionId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class SubscriptionIdRedisStringWriter implements
        Converter<SubscriptionId, String> {

    @Override
    public String convert(SubscriptionId source) {
        return String.format("%s:%s",
                source.getNodeId(),
                source.getQueryPayloadHash()
        );
    }
}
