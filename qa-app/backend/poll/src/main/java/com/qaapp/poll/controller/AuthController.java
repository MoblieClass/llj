package com.qaapp.poll.controller;

import com.qaapp.poll.dto.AuthRequest;
import com.qaapp.poll.dto.AuthResponse;
import com.qaapp.poll.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class AuthController {
    
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
    
    /**
     * 用户登出
     * 注意: 由于使用简单令牌，实际登出操作在前端完成（清除令牌）
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // 如果使用JWT + Cookie，这里可以清除Cookie
        return ResponseEntity.ok().build();
    }
}