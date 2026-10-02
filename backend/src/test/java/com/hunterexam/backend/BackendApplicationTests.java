package com.hunterexam.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.hunterexam.backend.config.DataInitializer;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:context;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "app.admin.email=admin@example.test",
        "app.admin.password=test-only-password",
        "google.login.enabled=false"
})
class BackendApplicationTests {

    @MockitoBean DataInitializer dataInitializer;

	@Test
	void contextLoads() {
	}

}
