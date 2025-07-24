//package com.barrcon.patchy.config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import jakarta.annotation.PostConstruct;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//
//@Configuration
//@Order(1)
//public class EnvConfig {
//
//    @PostConstruct
//    public void loadEnv() {
//        System.out.println("Loading environment variables from app.env...");
//        Dotenv dotenv = Dotenv.configure()
//            .filename("app.env")
//            .ignoreIfMissing()
//            .load();
//
//    String apiKey = dotenv.get("GOOGLE_API_KEY");
//        System.out.println("API Key from .env: " + (apiKey != null ? "SET (length: " + apiKey.length() + ")" : "NOT SET"));
//
//        if (apiKey != null) {
//            System.setProperty("GOOGLE_API_KEY", apiKey);
//            System.out.println("Set system property GOOGLE_API_KEY");
//    }
//}
//}