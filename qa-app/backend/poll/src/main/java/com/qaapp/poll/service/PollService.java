package com.qaapp.poll.service;

import com.qaapp.poll.dto.*;
import com.qaapp.poll.entity.Poll;
import com.qaapp.poll.entity.PollOption;
import com.qaapp.poll.exception.ResourceNotFoundException;
import com.qaapp.poll.repository.PollOptionRepository;
import com.qaapp.poll.repository.PollRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository optionRepository;

    @Autowired
    public PollService(PollRepository pollRepository, PollOptionRepository optionRepository) {
        this.pollRepository = pollRepository;
        this.optionRepository = optionRepository;
    }

    // 获取所有投票
    public List<PollDTO> getAllPolls() {
        return pollRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 根据ID获取投票
    public PollDTO getPollById(Long id) {
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的投票"));
        return convertToDTO(poll);
    }

    // 创建新投票
    @Transactional
    public PollDTO createPoll(CreatePollRequest request) {
        Poll poll = new Poll();

        poll.setTitle(request.getTitle());
        poll.setDescription(request.getDescription());
        poll.setCreatedAt(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now());
        poll.setEndDate(request.getEndDate());
        poll.setActive(true);

        // 创建选项
        request.getOptions().forEach(optionRequest -> {
            PollOption option = new PollOption();
            option.setContent(optionRequest.getContent());
            option.setVotes(0);
            poll.addOption(option);
        });

        Poll savedPoll = pollRepository.save(poll);
        return convertToDTO(savedPoll);
    }

    // 更新投票
    @Transactional
    public PollDTO updatePoll(Long id, UpdatePollRequest request) {
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的投票"));

        poll.setTitle(request.getTitle());
        poll.setDescription(request.getDescription());
        poll.setEndDate(request.getEndDate());
        poll.setActive(request.isActive());

        // 清除旧选项并添加新选项
        poll.getOptions().clear();

        request.getOptions().forEach(optionRequest -> {
            PollOption option;
            if (optionRequest.getId() != null) {
                option = optionRepository.findById(optionRequest.getId())
                        .orElseGet(PollOption::new);
            } else {
                option = new PollOption();
            }
            
            option.setContent(optionRequest.getContent());
            option.setVotes(optionRequest.getVotes());
            poll.addOption(option);
        });

        Poll updatedPoll = pollRepository.save(poll);
        return convertToDTO(updatedPoll);
    }

    // 删除投票
    @Transactional
    public void deletePoll(Long id) {
        if (!pollRepository.existsById(id)) {
            throw new ResourceNotFoundException("未找到ID为" + id + "的投票");
        }
        pollRepository.deleteById(id);
    }

    // 投票
    @Transactional
    public PollDTO vote(Long pollId, VoteRequest request) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + pollId + "的投票"));

        if (!poll.isActive()) {
            throw new IllegalStateException("该投票已关闭，无法投票");
        }

        if (poll.getEndDate() != null && LocalDateTime.now().isAfter(poll.getEndDate())) {
            throw new IllegalStateException("该投票已过期，无法投票");
        }

        PollOption option = poll.getOptions().stream()
                .filter(opt -> opt.getId().equals(request.getOptionId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + request.getOptionId() + "的选项"));

        option.setVotes(option.getVotes() + 1);
        optionRepository.save(option);

        return convertToDTO(poll);
    }

    // 搜索投票
    public List<PollDTO> searchPollsByTitle(String title) {
        return pollRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // 将实体转换为DTO
    public PollDTO convertToDTO(Poll poll) {
        PollDTO dto = new PollDTO();
        dto.setId(poll.getId());
        dto.setTitle(poll.getTitle());
        dto.setDescription(poll.getDescription());
        dto.setCreatedAt(poll.getCreatedAt());
        dto.setEndDate(poll.getEndDate());
        dto.setActive(poll.isActive());

        List<PollOptionDTO> optionDTOs = poll.getOptions().stream()
                .map(option -> {
                    PollOptionDTO optionDTO = new PollOptionDTO();
                    optionDTO.setId(option.getId());
                    optionDTO.setContent(option.getContent());
                    optionDTO.setVotes(option.getVotes());
                    return optionDTO;
                })
                .collect(Collectors.toList());

        dto.setOptions(optionDTOs);
        return dto;
    }
}