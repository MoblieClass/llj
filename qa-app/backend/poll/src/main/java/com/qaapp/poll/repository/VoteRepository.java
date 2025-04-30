package com.qaapp.poll.repository;

import com.qaapp.poll.entity.Poll;
import com.qaapp.poll.entity.User;
import com.qaapp.poll.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    boolean existsByUserAndPoll(User user, Poll poll);
    
    List<Vote> findByUserId(Long userId);
    
    List<Vote> findByPollId(Long pollId);
}