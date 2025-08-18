package com.example.demo.controllers;

import com.example.demo.entities.RepositoryEntity;
import com.example.demo.services.RepositoryQueryService;
import com.example.demo.services.RepositoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class RepositoryController {
    private final RepositoryService repositoryService;
    private final RepositoryQueryService repositoryQueryService;

    public RepositoryController(RepositoryService repositoryService, RepositoryQueryService repositoryQueryService) {
        this.repositoryService = repositoryService;
        this.repositoryQueryService = repositoryQueryService;
    }

     //POST endpoint where request body has query, language, sort
    @PostMapping("/search")
    public ResponseEntity<?> searchRepositories(@RequestBody Map<String, String> requestBody) {
        String query = requestBody.get("query");
        String language = requestBody.get("language");
        String sort = requestBody.get("sort");

        Map<String, Object> response = new HashMap<>();
        //input validation
        if (query == null || query.isBlank()) {
            response.put("message", "Query parameter is required");
            response.put("repositories", Collections.emptyList());
            response.put("count", 0);
            return ResponseEntity.badRequest().body(response);
        }
        //response.put("repositories",results);
        Map<String, Object> results = repositoryService.searchRepositories(query, language, sort);
        results.put("count",results.size());

        return ResponseEntity.ok(results);
    }

    // GET endpoint to fetch repositories from DB
    @GetMapping("/repositories")
    public ResponseEntity<?> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Long minStars,
            @RequestParam(required = false, defaultValue = "stars") String sort
    ) {
        //List<RepositoryEntity> repos = repositoryQueryService.getRepositories(language, minStars, sort);
        Map<String, Object> response = new HashMap<>();
        try {
            // Validation
            if (minStars != null && minStars < 0) {
                response.put("message", "minStars must be a non-negative number.");
                response.put("count", 0);
                response.put("results", Collections.emptyList());
                return ResponseEntity.badRequest().body(response);
            }

            if (!sort.equals("stars") && !sort.equals("forks") && !sort.equals("updated")) {
                response.put("message", "Invalid sort parameter. Allowed values: stars, forks, updated.");
                response.put("count", 0);
                response.put("results", Collections.emptyList());
                return ResponseEntity.badRequest().body(response);
            }

            // Fetch data
            List<RepositoryEntity> repos = repositoryQueryService.getRepositories(language, minStars, sort);

            response.put("message", repos.isEmpty() ? "No repositories found." : "Repositories fetched successfully.");
            response.put("count", repos.size());
            response.put("results", repos);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "An error occurred while fetching repositories.");
            response.put("error", e.getMessage());
            response.put("count", 0);
            response.put("results", Collections.emptyList());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
