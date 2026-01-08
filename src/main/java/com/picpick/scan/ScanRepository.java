package com.picpick.scan;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanRepository extends JpaRepository<Scan, Long> {
    @EntityGraph(attributePaths = { "user", "mart", "gemini" })
    java.util.List<Scan> findAllByUser_Id(Long userId);
}
