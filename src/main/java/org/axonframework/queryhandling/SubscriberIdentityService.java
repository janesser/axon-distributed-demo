package org.axonframework.queryhandling;

import java.lang.management.ManagementFactory;

public class SubscriberIdentityService {

    /**
     * @see org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore.Builder JpaTokenStore (Builder) nodeId
     */
    public String getSubscriberIdentify() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }
}
