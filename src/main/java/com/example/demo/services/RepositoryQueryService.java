package com.example.demo.services;

import com.example.demo.entities.RepositoryEntity;
import com.example.demo.repositories.RepositoryRepo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RepositoryQueryService {

    private final RepositoryRepo repositoryRepo;

    public RepositoryQueryService(RepositoryRepo repositoryRepo) {
        this.repositoryRepo = repositoryRepo;
    }

    // Fetch repositories with filters + sorting
    public List<RepositoryEntity> getRepositories(String language, Long minStars, String sort) {
        // Defensive check: repositoryRepo must not be null
        if (repositoryRepo == null) {
            throw new IllegalStateException("Repository repository cannot be null.");
        }

        List<RepositoryEntity> repositories = repositoryRepo.findAll();

        if (repositories.isEmpty()) {
            throw new IllegalStateException("No repositories found in database.");
        }

        // Validate minStars
        if (minStars != null && minStars < 0) {
            throw new IllegalArgumentException("minStars must be non-negative.");
        }

        // Apply filters
        if (language != null && !language.isBlank()) {
            repositories = repositories.stream()
                    .filter(repo -> repo.getLanguage() != null &&
                            language.equalsIgnoreCase(repo.getLanguage()))
                    .collect(Collectors.toList());
        }

        if (minStars != null) {
            repositories = repositories.stream()
                    .filter(repo -> repo.getStars() >= minStars)
                    .collect(Collectors.toList());
        }

        if (repositories.isEmpty()) {
            throw new IllegalStateException("No repositories matched the given filters.");
        }

        // Sorting
        Comparator<RepositoryEntity> comparator;
        if (sort == null || sort.isBlank()) {
            sort = "stars"; // default
        }

        comparator = switch (sort.toLowerCase()) {
            case "forks" -> Comparator.comparingLong(RepositoryEntity::getForks);
            case "updated" -> Comparator.comparing(repo -> Objects.requireNonNull(repo.getLastUpdated()));
            default -> Comparator.comparingLong(RepositoryEntity::getStars);
        };

        return repositories.stream()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
    }
}
