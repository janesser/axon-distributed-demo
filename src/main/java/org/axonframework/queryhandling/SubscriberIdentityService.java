package org.axonframework.queryhandling;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class SubscriberIdentityService {

    /**
     * @see org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore.Builder JpaTokenStore (Builder) nodeId
     */
    public String getSubscriberIdentify() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }
}
