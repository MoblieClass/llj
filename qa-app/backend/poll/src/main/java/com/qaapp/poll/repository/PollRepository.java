package com.qaapp.poll.repository;

import com.qaapp.poll.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    
    List<Poll> findByIsActiveTrue();
    
    List<Poll> findByTitleContainingIgnoreCase(String title);
    
    @Query("SELECT p FROM Poll p WHERE p.endDate < :now AND p.isActive = true")
    List<Poll> findExpiredActivePolls(LocalDateTime now);
}