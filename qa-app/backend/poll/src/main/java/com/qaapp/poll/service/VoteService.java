package com.qaapp.poll.service;

import com.qaapp.poll.dto.PollDTO;
import com.qaapp.poll.dto.VoteRequest;
import com.qaapp.poll.entity.Poll;
import com.qaapp.poll.entity.PollOption;
import com.qaapp.poll.entity.User;
import com.qaapp.poll.entity.Vote;
import com.qaapp.poll.exception.ResourceNotFoundException;
import com.qaapp.poll.repository.PollOptionRepository;
import com.qaapp.poll.repository.PollRepository;
import com.qaapp.poll.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VoteService {
    
    private final PollRepository pollRepository;
    private final PollOptionRepository optionRepository;
    private final VoteRepository voteRepository;
    private final PollService pollService;
    
    @Autowired
    public VoteService(
            PollRepository pollRepository,
            PollOptionRepository optionRepository,
            VoteRepository voteRepository,
            PollService pollService) {
        this.pollRepository = pollRepository;
        this.optionRepository = optionRepository;
        this.voteRepository = voteRepository;
        this.pollService = pollService;
    }
    
    /**
     * 用户投票
     */
    @Transactional
    public PollDTO vote(Long pollId, VoteRequest voteRequest, User user) {
        // 检查投票是否存在
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + pollId + "的投票"));
        
        // 检查投票是否激活
        if (!poll.isActive()) {
            throw new IllegalStateException("该投票已关闭，无法投票");
        }
        
        // 检查投票是否过期
        if (poll.getEndDate() != null && LocalDateTime.now().isAfter(poll.getEndDate())) {
            throw new IllegalStateException("该投票已过期，无法投票");
        }
        
        // 检查用户是否已经投过票
        if (voteRepository.existsByUserAndPoll(user, poll)) {
            throw new IllegalStateException("您已经对此投票进行了投票");
        }
        
        // 检查选项是否存在
        PollOption option = optionRepository.findById(voteRequest.getOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + voteRequest.getOptionId() + "的选项"));
        
        // 检查选项是否属于当前投票
        if (!option.getPoll().getId().equals(pollId)) {
            throw new IllegalArgumentException("选项不属于该投票");
        }
        
        // 记录投票
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setPoll(poll);
        vote.setOption(option);
        voteRepository.save(vote);
        
        // 更新选项票数
        option.setVotes(option.getVotes() + 1);
        optionRepository.save(option);
        
        // 返回更新后的投票
        return pollService.convertToDTO(poll);
    }
    
    /**
     * 检查用户是否已投票
     */
    public boolean hasUserVoted(Long pollId, Long userId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + pollId + "的投票"));
        
        User user = new User();
        user.setId(userId);
        
        return voteRepository.existsByUserAndPoll(user, poll);
    }
}