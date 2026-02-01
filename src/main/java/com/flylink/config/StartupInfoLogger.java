package com.flylink.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Exibe informações ao iniciar a aplicação.
 */
@Slf4j
@Component
public class StartupInfoLogger {

    @Value("${server.port:8080}")
    private int port;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        log.info("API running at: http://localhost:{}", port);
        log.info("Docs: http://localhost:{}/docs.html", port);
    }
}
