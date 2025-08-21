package com.example.demo.controllers;

import com.example.demo.entities.RepositoryEntity;
import com.example.demo.services.RepositoryQueryService;
import com.example.demo.services.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryControllerTest {

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private RepositoryQueryService repositoryQueryService;

    @InjectMocks
    private RepositoryController repositoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //  Test POST /search with valid input
    @Test
    void testSearchRepositories_ValidRequest() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", "spring");
        requestBody.put("language", "java");
        requestBody.put("sort", "stars");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("repositories", List.of(new RepositoryEntity()));
        mockResponse.put("count", 1);

        when(repositoryService.searchRepositories("spring", "java", "stars"))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = repositoryController.searchRepositories(requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("count"));
    }

    //  Test POST /search with missing query
    @Test
    void testSearchRepositories_MissingQuery() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("language", "java");
        requestBody.put("sort", "stars");

        ResponseEntity<?> response = repositoryController.searchRepositories(requestBody);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Query parameter is required",
                ((Map<?, ?>) response.getBody()).get("message"));
    }

    // ✅ Test GET /repositories valid request
    @Test
    void testGetRepositories_ValidRequest() {
        RepositoryEntity repo = new RepositoryEntity();
        repo.setId(1L);
        repo.setName("Test Repo");

        when(repositoryQueryService.getRepositories("java", 10L, "stars"))
                .thenReturn(List.of(repo));

        ResponseEntity<?> response = repositoryController.getRepositories("java", 10L, "stars");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals(1, responseBody.get("count"));
        assertEquals("Repositories fetched successfully.", responseBody.get("message"));
    }

    // Test GET /repositories with invalid minStars
    @Test
    void testGetRepositories_InvalidMinStars() {
        ResponseEntity<?> response = repositoryController.getRepositories("java", -5L, "stars");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("minStars must be a non-negative number.",
                ((Map<?, ?>) response.getBody()).get("message"));
    }

    // Test GET /repositories with invalid sort parameter
    @Test
    void testGetRepositories_InvalidSort() {
        ResponseEntity<?> response = repositoryController.getRepositories("java", 5L, "invalidSort");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid sort parameter. Allowed values: stars, forks, updated.",
                ((Map<?, ?>) response.getBody()).get("message"));
    }

    // Test GET /repositories when service throws exception
    @Test
    void testGetRepositories_ServiceException() {
        when(repositoryQueryService.getRepositories("java", 5L, "stars"))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = repositoryController.getRepositories("java", 5L, "stars");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while fetching repositories.",
                ((Map<?, ?>) response.getBody()).get("message"));
    }
}
