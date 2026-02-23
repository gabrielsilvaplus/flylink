package com.flylink.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "JWT_SECRET=this-is-a-strong-secret-key-for-jwt-testing-only",
        "JWT_EXPIRATION_MS=86400000"
})
public abstract class AbstractIntegrationTest {

}
