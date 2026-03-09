package com.alessandro.congress_management;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CongressManagementApplication {

    public static void main(String[] args) {
        // Cargar .env solo si existe (desarrollo local)
        // En produccion Docker inyecta las variables directamente
        if (new java.io.File(".env").exists()) {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            loadEnvVar(dotenv, "JWT_SECRET");
            loadEnvVar(dotenv, "JWT_EXPIRATION");
            loadEnvVar(dotenv, "JWT_REFRESH_EXPIRATION");
            loadEnvVar(dotenv, "DB_URL");
            loadEnvVar(dotenv, "DB_USERNAME");
            loadEnvVar(dotenv, "DB_PASSWORD");
            loadEnvVar(dotenv, "AWS_ACCESS_KEY");
            loadEnvVar(dotenv, "AWS_SECRET_KEY");
            loadEnvVar(dotenv, "AWS_REGION");
            loadEnvVar(dotenv, "AWS_S3_BUCKET_NAME");
            loadEnvVar(dotenv, "MAIL_USERNAME");
            loadEnvVar(dotenv, "MAIL_APP_PASSWORD");
            loadEnvVar(dotenv, "MAIL_FROM_NAME");
            loadEnvVar(dotenv, "APP_FRONTEND_URL");
        }
        SpringApplication.run(CongressManagementApplication.class, args);
    }

    private static void loadEnvVar(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null) System.setProperty(key, value);
    }
}


