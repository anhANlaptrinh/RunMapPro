package com.example.runningapp.common;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test")
public class MongoConnectionVerifier implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final String configuredMongoUri;

    public MongoConnectionVerifier(MongoTemplate mongoTemplate,
            @Value("${MONGO_URI:}") String configuredMongoUri) {
        this.mongoTemplate = mongoTemplate;
        this.configuredMongoUri = configuredMongoUri;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (configuredMongoUri == null || configuredMongoUri.isBlank()) {
            log.debug("Skipping MongoDB connection verification because MONGO_URI is not configured.");
            return;
        }
        try {
            mongoTemplate.executeCommand(new Document("ping", 1));
            log.info("MongoDB connected successfully!");
        } catch (Exception ex) {
            log.warn("MongoDB connection check failed: {}", ex.getMessage());
        }
    }
}
