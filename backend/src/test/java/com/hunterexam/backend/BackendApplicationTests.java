package com.hunterexam.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.hunterexam.backend.config.DataInitializer;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @MockitoBean DataInitializer dataInitializer;

	@Test
	void contextLoads() {
	}

}
