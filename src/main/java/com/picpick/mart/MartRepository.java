package com.picpick.mart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface MartRepository extends JpaRepository<Mart, Long> {
    @Query(value = "SELECT * FROM marts m " +
            "WHERE ST_Distance_Sphere(point(m.longitude, m.latitude), point(:longitude, :latitude)) <= 10 "
            +
            "ORDER BY ST_Distance_Sphere(point(m.longitude, m.latitude), point(:longitude, :latitude)) ASC", nativeQuery = true)
    List<Mart> findNearbyMart(@Param("longitude") Double longitude, @Param("latitude") Double latitude);

    Optional<Mart> findByRegistrationNumber(BigInteger registrationNumber);
}
