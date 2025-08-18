package com.example.demo.services;

import com.example.demo.entities.RepositoryEntity;
import com.example.demo.repositories.RepositoryRepo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepositoryQueryService {

    private final RepositoryRepo repositoryRepo;

    public RepositoryQueryService(RepositoryRepo repositoryRepo) {
        this.repositoryRepo = repositoryRepo;
    }

    // Fetch repositories with filters + sorting
    public List<RepositoryEntity> getRepositories(String language, Long minStars, String sort) {
        List<RepositoryEntity> repositories = repositoryRepo.findAll();

        // Filter by language
        if (language != null && !language.isBlank()) {
            repositories = repositories.stream()
                    .filter(repo -> language.equalsIgnoreCase(repo.getLanguage()))
                    .collect(Collectors.toList());
        }

        // Filter by minStars
        if (minStars != null) {
            repositories = repositories.stream()
                    .filter(repo -> repo.getStars() >= minStars)
                    .collect(Collectors.toList());
        }

        // Sorting
        Comparator<RepositoryEntity> comparator;
        switch (sort.toLowerCase()) {
            case "forks":
                comparator = Comparator.comparingLong(RepositoryEntity::getForks).reversed();
                break;
            case "updated":
                comparator = Comparator.comparing(RepositoryEntity::getLastUpdated).reversed();
                break;
            case "stars":
            default:
                comparator = Comparator.comparingLong(RepositoryEntity::getStars).reversed();
        }

        return repositories.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
