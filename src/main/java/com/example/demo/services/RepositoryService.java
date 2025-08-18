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
    int count=0;

    public RepositoryService(RepositoryRepo repositoryRepo){
        this.repositoryRepo = repositoryRepo;
    }

    public Map<String, Object> searchRepositories(String query, String language, String sort) {
        Map<String, Object> responseMap = new HashMap<>();
        List<RepositoryEntity> result = new ArrayList<>();

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
                responseMap.put("message", "GitHub API returned error: " + response.getStatusCode());
                responseMap.put("repositories", Collections.emptyList());
                responseMap.put("count", 0);
                return responseMap;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            for (JsonNode item : items) {
                Long repoId = item.get("id").asLong();
                RepositoryEntity entity = repositoryRepo.findById(repoId).orElse(new RepositoryEntity());
                entity.setId(item.get("id").asLong());
                entity.setName(item.get("name").asText());
                entity.setOwner(item.get("owner").get("login").asText());
                entity.setDescription(item.get("description").asText(""));
                entity.setLanguage(item.get("language").asText(""));
                entity.setStars(item.get("stargazers_count").asLong());
                entity.setForks(item.get("forks_count").asLong());
                entity.setLastUpdated(OffsetDateTime.parse(item.get("updated_at").asText()));

                result.add(entity);
                repositoryRepo.save(entity);
                count++;
            }

            // Save in DB
            //repositoryRepo.saveAll(result);
            System.out.println("count = "+count);

            responseMap.put("message", "Repositories fetched from GitHub API and saved to DB successfully");
            responseMap.put("repositories", result);
            responseMap.put("count", Integer.toString(count));

        }  catch (Exception e) {
            responseMap.put("message", "Unexpected error: " + e.getMessage());
            responseMap.put("repositories", Collections.emptyList());
            responseMap.put("count", 0);
        }

        return responseMap;
    }
}
