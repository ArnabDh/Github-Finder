package com.example.demo.repositories;

import com.example.demo.entities.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryRepo extends JpaRepository<RepositoryEntity, Long> {

    // Handy finder for your GET /repositories later
    List<RepositoryEntity> findAllByLanguageIgnoreCaseAndStarsGreaterThanEqualOrderByStarsDesc(
            String language, long minStars
    );

    // You can add more variants as you need (e.g., sorting by forks or updated)
}