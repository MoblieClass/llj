package com.qaapp.poll.service;

import com.qaapp.poll.dto.AuthRequest;
import com.qaapp.poll.dto.AuthResponse;
import com.qaapp.poll.entity.User;
import com.qaapp.poll.config.CustomPasswordEncoder;
import com.qaapp.poll.exception.ResourceNotFoundException;
import com.qaapp.poll.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;



@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final CustomPasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, CustomPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * 注册新用户
     */
    public AuthResponse register(AuthRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole("ROLE_USER");
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 生成简单令牌 (实际应用中应使用JWT)
        String simpleToken = UUID.randomUUID().toString();
        
        // 返回认证响应
        return new AuthResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                simpleToken,
                savedUser.getRole()
        );
    }
    
    /**
     * 用户登录
     */
    public AuthResponse login(AuthRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        
        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成简单令牌 (实际应用中应使用JWT)
        String simpleToken = UUID.randomUUID().toString();
        
        // 返回认证响应
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                simpleToken,
                user.getRole()
        );
    }
    
    /**
     * 根据ID查找用户
     */
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }
}