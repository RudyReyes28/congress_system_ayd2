package com.alessandro.congress_management;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CongressManagementApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();

        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));
        System.setProperty("JWT_REFRESH_EXPIRATION", dotenv.get("JWT_REFRESH_EXPIRATION"));

        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        System.setProperty("AWS_ACCESS_KEY", dotenv.get("AWS_ACCESS_KEY"));
        System.setProperty("AWS_SECRET_KEY", dotenv.get("AWS_SECRET_KEY"));
        System.setProperty("AWS_REGION", dotenv.get("AWS_REGION"));
        System.setProperty("AWS_S3_BUCKET_NAME", dotenv.get("AWS_S3_BUCKET_NAME"));

        System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
        System.setProperty("MAIL_APP_PASSWORD", dotenv.get("MAIL_APP_PASSWORD"));
        System.setProperty("MAIL_FROM_NAME", dotenv.get("MAIL_FROM_NAME"));

		SpringApplication.run(CongressManagementApplication.class, args);
	}

}
