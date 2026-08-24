package com.marafis.nii.alert.consumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * IncidentRepository - JPA repository for Incident entity persistence
 *
 * Provides access to incidents stored in PostgreSQL incidents table
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    /**
     * Find incident by alert ID
     */
    Optional<Incident> findByAlertId(String alertId);

    /**
     * Find all open incidents
     */
    @Query("SELECT i FROM Incident i WHERE i.status = 'OPEN' ORDER BY i.severityScore DESC, i.createdAt DESC")
    List<Incident> findAllOpen();

    /**
     * Find incidents by severity level
     */
    List<Incident> findBySeverity(String severity);

    /**
     * Find incidents by component
     */
    List<Incident> findByComponent(String component);

    /**
     * Find critical incidents (severity_score >= 80)
     */
    @Query("SELECT i FROM Incident i WHERE i.severityScore >= 80 AND i.status = 'OPEN'")
    List<Incident> findCriticalOpenIncidents();

    /**
     * Find incidents in a region
     */
    List<Incident> findByRegion(String region);

    /**
     * Count incidents by status
     */
    long countByStatus(String status);

    /**
     * Count critical incidents
     */
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.severityScore >= 80 AND i.status = 'OPEN'")
    long countCriticalOpen();

    /**
     * Find incidents created in the last N minutes
     */
    @Query("SELECT i FROM Incident i WHERE i.createdAt >= :since ORDER BY i.createdAt DESC")
    List<Incident> findRecentIncidents(@Param("since") Instant since);

    /**
     * Find incidents with runbook reference
     */
    @Query("SELECT i FROM Incident i WHERE i.hasRunbook = true AND i.status = 'OPEN'")
    List<Incident> findOpenWithRunbook();
}