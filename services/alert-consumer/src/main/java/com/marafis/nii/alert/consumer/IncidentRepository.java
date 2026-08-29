package com.marafis.nii.alert.consumer;

import com.marafis.nii.alert.consumer.domain.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByAlertId(String alertId);

    @Query("SELECT i FROM Incident i WHERE i.status = 'OPEN' ORDER BY i.severityScore DESC, i.createdAt DESC")
    List<Incident> findAllOpen();

    List<Incident> findBySeverity(String severity);

    List<Incident> findByComponent(String component);

    @Query("SELECT i FROM Incident i WHERE i.severityScore >= 80 AND i.status = 'OPEN'")
    List<Incident> findCriticalOpenIncidents();

    List<Incident> findByRegion(String region);

    long countByStatus(String status);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.severityScore >= 80 AND i.status = 'OPEN'")
    long countCriticalOpen();

    @Query("SELECT i FROM Incident i WHERE i.createdAt >= :since ORDER BY i.createdAt DESC")
    List<Incident> findRecentIncidents(@Param("since") Instant since);

    @Query("SELECT i FROM Incident i WHERE i.hasRunbook = true AND i.status = 'OPEN'")
    List<Incident> findOpenWithRunbook();
}

