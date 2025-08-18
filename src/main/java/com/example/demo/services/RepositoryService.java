package com.example.demo.services;

import com.example.demo.entities.RepositoryEntity;
import com.example.demo.repositories.RepositoryRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class RepositoryService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RepositoryRepo repositoryRepo;

    public RepositoryService(RepositoryRepo repositoryRepo){
        this.repositoryRepo = repositoryRepo;

    }


    public List<RepositoryEntity> searchRepositories(String query, String language, String sort) {
        try {
            // Build GitHub API URL dynamically
            StringBuilder urlBuilder = new StringBuilder("https://api.github.com/search/repositories?q=");
            urlBuilder.append(query);
            if (language != null && !language.isBlank()) {
                urlBuilder.append("+language:").append(language);
            }
            if (sort != null && !sort.isBlank()) {
                urlBuilder.append("&sort=").append(sort);
            }

            String url = urlBuilder.toString();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("GitHub API error: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            //List<Map<String, Object>> result = new ArrayList<>();
            List<RepositoryEntity> result = new ArrayList<>();
            for (JsonNode item : items) {
                RepositoryEntity entity = new RepositoryEntity();
                entity.setId(item.get("id").asLong());
                entity.setName(item.get("name").asText());
                entity.setOwner(item.get("owner").get("login").asText());
                entity.setDescription(item.get("description").asText(""));
                entity.setLanguage(item.get("language").asText(""));
                entity.setStars(item.get("stargazers_count").asLong());
                entity.setForks(item.get("forks_count").asLong());
                // Parse ISO date string into OffsetDateTime
                entity.setLastUpdated(OffsetDateTime.parse(item.get("updated_at").asText()));

                result.add(entity);
            }

            // Save in DB
            repositoryRepo.saveAll(result);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching GitHub data: " + e.getMessage(), e);
        }
    }
}
