package io.spring.billrun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@SpringBootApplication
@EnableTask
public class BillRunApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillRunApplication.class, args);
	}

}
