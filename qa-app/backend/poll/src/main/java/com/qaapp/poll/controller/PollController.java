package com.qaapp.poll.controller;

import com.qaapp.poll.dto.*;
import com.qaapp.poll.entity.User;
import com.qaapp.poll.service.UserService;
import com.qaapp.poll.service.VoteService;
import com.qaapp.poll.service.PollService;
import com.qaapp.poll.service.UserService;
import com.qaapp.poll.service.VoteService;
import com.qaapp.poll.util.ExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/polls")
@CrossOrigin(origins = "*") // 或者指定前端域名
public class PollController {

    private final PollService pollService;
    private final ExportUtil exportUtil;

    private final VoteService voteService;
    private final UserService userService;

    @Autowired
    public PollController(PollService pollService, ExportUtil exportUtil, VoteService voteService, UserService userService) {
        this.pollService = pollService;
        this.exportUtil = exportUtil;
        this.voteService = voteService;
        this.userService = userService;
    }

    
    

    // 获取所有投票
    @GetMapping
    public ResponseEntity<List<PollDTO>> getAllPolls() {
        return ResponseEntity.ok(pollService.getAllPolls());
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<PollDTO> vote(
            @PathVariable Long id,
            @Valid @RequestBody VoteRequest request,
            @RequestHeader("User-Id") Long userId) {
        
        // 获取用户
        User user = userService.findUserById(userId);
        
        // 进行投票
        PollDTO updatedPoll = voteService.vote(id, request, user);
        
        return ResponseEntity.ok(updatedPoll);
    }
    
    /**
     * 检查用户是否已投票
     */
    @GetMapping("/{id}/has-voted")
    public ResponseEntity<Boolean> hasVoted(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        
        boolean hasVoted = voteService.hasUserVoted(id, userId);
        
        return ResponseEntity.ok(hasVoted);
    }

    // 根据ID获取投票
    @GetMapping("/{id}")
    public ResponseEntity<PollDTO> getPollById(@PathVariable Long id) {
        return ResponseEntity.ok(pollService.getPollById(id));
    }

    // 创建投票
    @PostMapping
    public ResponseEntity<PollDTO> createPoll(@Valid @RequestBody CreatePollRequest request) {
        return new ResponseEntity<>(pollService.createPoll(request), HttpStatus.CREATED);
    }

    // 更新投票
    @PutMapping("/{id}")
    public ResponseEntity<PollDTO> updatePoll(@PathVariable Long id, @Valid @RequestBody UpdatePollRequest request) {
        return ResponseEntity.ok(pollService.updatePoll(id, request));
    }

    // 删除投票
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoll(@PathVariable Long id) {
        pollService.deletePoll(id);
        return ResponseEntity.noContent().build();
    }


    // 搜索投票
    @GetMapping("/search")
    public ResponseEntity<List<PollDTO>> searchPolls(@RequestParam String title) {
        return ResponseEntity.ok(pollService.searchPollsByTitle(title));
    }

    // 导出CSV
    @GetMapping("/{id}/export/csv")
    public void exportToCsv(@PathVariable Long id, HttpServletResponse response) throws IOException {
        PollDTO poll = pollService.getPollById(id);
        exportUtil.exportPollToCsv(poll, response);
    }

    // 导出Excel
    @GetMapping("/{id}/export/excel")
    public void exportToExcel(@PathVariable Long id, HttpServletResponse response) throws IOException {
        PollDTO poll = pollService.getPollById(id);
        exportUtil.exportPollToExcel(poll, response);
    }
}