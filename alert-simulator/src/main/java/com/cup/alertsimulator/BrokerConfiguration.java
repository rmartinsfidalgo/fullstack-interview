package com.cup.alertsimulator;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.springframework.boot.artemis.autoconfigure.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the embedded Artemis broker:
 * - Adds a TCP acceptor on port 61616 for the candidate's app
 * - Routes failed messages to DLQ after 3 delivery attempts
 */
@Configuration
class BrokerConfiguration implements ArtemisConfigurationCustomizer {

    @Override
    public void customize(org.apache.activemq.artemis.core.config.Configuration configuration) {
        try {
            configuration.setJMXManagementEnabled(false);
            configuration.addAcceptorConfiguration("netty", "tcp://0.0.0.0:61616");
            configuration.addAddressSetting("#",
                    new AddressSettings()
                            .setDeadLetterAddress(SimpleString.of("DLQ"))
                            .setMaxDeliveryAttempts(3)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure embedded Artemis broker", e);
        }
    }
}
