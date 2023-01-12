package com.alicetechnologies.planner;

import com.alicetechnologies.planner.task.TaskRepository;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PlannerApplication {

	public static final String API_PLAN = "/api/plan";
	public static final String API_TASK = "/api/task";

	public static void main(String[] args) {
		SpringApplication.run(PlannerApplication.class, args);
	}

	@Bean
	public CriticalPathEngine criticalPathEngine(final TaskRepository taskRepository) {
		return new CriticalPathEngine(taskRepository.getTasks());
	}

	@Bean
	public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
		return new OpenAPI()
			.components(new Components())
			.info(new Info().title("Planner API").version(appVersion)
				.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}
}
