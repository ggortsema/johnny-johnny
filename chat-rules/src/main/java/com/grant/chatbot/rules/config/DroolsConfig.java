package com.grant.chatbot.rules.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;

public final class DroolsConfig {
    private DroolsConfig() {
    }

    public static KieContainer kieContainer() {
        return KieServices.Factory.get().getKieClasspathContainer();
    }
}
