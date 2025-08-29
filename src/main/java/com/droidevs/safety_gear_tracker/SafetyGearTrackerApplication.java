package com.droidevs.safety_gear_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class SafetyGearTrackerApplication {

  public static void main(String[] args) {
    SpringApplication.run(SafetyGearTrackerApplication.class, args);
  }

}
