package com.picpick.repositories;

import com.picpick.entities.Mart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MartRepository extends JpaRepository<Mart, Long> {

        // MySQL ST_Distance_Sphere returns distance in meters
        @Query(value = "SELECT * FROM mart m " +
                        "WHERE ST_Distance_Sphere(point(m.longitude, m.latitude), point(:longitude, :latitude)) <= 10 "
                        +
                        "ORDER BY ST_Distance_Sphere(point(m.longitude, m.latitude), point(:longitude, :latitude)) ASC", nativeQuery = true)
        List<Mart> findNearestMart(
                        @Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        Pageable pageable);
}
