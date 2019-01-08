package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"controller", "service", "impl","configuration", "interceptors", "util"})
public class JwtInterceptorApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtInterceptorApplication.class, args);
	}
}
