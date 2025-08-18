package com.example.demo.controllers;

import com.example.demo.entities.RepositoryEntity;
import com.example.demo.services.RepositoryQueryService;
import com.example.demo.services.RepositoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        List<RepositoryEntity> results = repositoryService.searchRepositories(query, language, sort);
        Map<String, Object> response = new HashMap<>();
        response.put("repositories",results);
        response.put("count",results.size());

        return ResponseEntity.ok(response);
    }

    // GET endpoint to fetch repositories from DB
    @GetMapping("/repositories")
    public ResponseEntity<?> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Long minStars,
            @RequestParam(required = false, defaultValue = "stars") String sort
    ) {
        List<RepositoryEntity> repos = repositoryQueryService.getRepositories(language, minStars, sort);
        Map<String, Object> response = new HashMap<>();
        response.put("count", repos.size());
        response.put("respositories", repos);

        return ResponseEntity.ok(response);
    }
}
