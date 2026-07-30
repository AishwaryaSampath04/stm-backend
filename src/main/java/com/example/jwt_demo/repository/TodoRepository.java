package com.example.jwt_demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.jwt_demo.model.Todo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    // Simple search by status
    List<Todo> findByStatus(String status);
    @Query("SELECT t FROM Todo t WHERE t.delete = false")
Page<Todo> findAllActive(Pageable pageable);

   


    // Search by title, description, tag, or status (case-insensitive)
    List<Todo> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTagContainingIgnoreCaseOrStatusContainingIgnoreCase(
            String title, String description, String tag, String status);

    // Same with pageable
    Page<Todo> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTagContainingIgnoreCaseOrStatusContainingIgnoreCase(
            String title, String description, String tag, String status, Pageable pageable);

    // Custom query for keyword + status filtering
    @Query("SELECT t FROM Todo t WHERE " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :key, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :key, '%')) OR " +
            "LOWER(t.tag) LIKE LOWER(CONCAT('%', :key, '%'))) AND " +
            "LOWER(t.status) = LOWER(:status)")
    Page<Todo> findByStatusAndKeyword(@Param("key") String key,
                                      @Param("status") String status,
                                      Pageable pageable);

    Page<Todo> findByStatusIgnoreCase(String status, Pageable pageable);
    
    // In TodoRepository


}
