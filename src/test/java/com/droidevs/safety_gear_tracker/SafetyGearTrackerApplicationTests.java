package com.droidevs.safety_gear_tracker;

import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class SafetyGearTrackerApplicationTests {

	@MockBean
	private GenerativeModel generativeModel;

	@Test
	void contextLoads() {
	}

}
