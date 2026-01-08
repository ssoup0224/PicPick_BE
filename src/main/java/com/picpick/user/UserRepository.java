package com.picpick.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = { "mart" })
    java.util.Optional<User> findWithMartById(Long id);

    Optional<User> findByUuid(String uuid);
}
