package com.picpick.api.gemini;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeminiRepository extends JpaRepository<Gemini, Long> {
    List<Gemini> findAllByUserId(Long userId);

    Optional<Gemini> findByScanId(Long scanId);

    Optional<Gemini> findFirstByScanNameOrderByIdDesc(String scanName);
}
