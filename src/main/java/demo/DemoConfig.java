package demo;

import org.axonframework.eventhandling.EventHandlerInvoker;
import org.axonframework.eventhandling.MultiEventHandlerInvoker;
import org.axonframework.eventhandling.async.FullConcurrencyPolicy;
import org.axonframework.eventhandling.async.SequencingPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfig {

    @Bean public SequencingPolicy sequencingPolicy() {
        return new FullConcurrencyPolicy();
    }

    @Bean public EventHandlerInvoker eventHandlerInvoker() {
        return new MultiEventHandlerInvoker();
    }
}
