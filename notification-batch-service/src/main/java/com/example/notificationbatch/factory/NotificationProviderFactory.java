package com.example.notificationbatch.factory;

import com.example.notificationbatch.provider.NotificationProvider;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import java.util.List;

// Simple registry that allows adding new providers without changing batch logic.
@Component
public class NotificationProviderFactory {

    private final Map<String, NotificationProvider> registry = new HashMap<>();

    public NotificationProviderFactory(List<NotificationProvider> providers) {
        // register all providers discovered by Spring
        if (providers != null) {
            for (NotificationProvider p : providers) {
                register(p);
            }
        }
    }

    public void register(NotificationProvider p) {
        registry.put(p.getMode(), p);
    }

    public NotificationProvider get(String mode) {
        return registry.get(mode);
    }
}
