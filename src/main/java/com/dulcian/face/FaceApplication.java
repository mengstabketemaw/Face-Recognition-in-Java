package com.dulcian.face;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class FaceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(FaceApplication.class, args);
	}


}
