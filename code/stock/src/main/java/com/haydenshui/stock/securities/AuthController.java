package com.haydenshui.stock.securities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.dto.auth.LoginDTO;
import com.haydenshui.stock.lib.dto.auth.RegisterDTO;
import com.haydenshui.stock.lib.util.ApiResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户认证控制器，提供登录、注册、登出等功能
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果，包含JWT令牌
     */
    @PostMapping("/login")
    @LogDetails
    public ResponseEntity<ApiResult<?>> login(@RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @LogDetails
    public ResponseEntity<ApiResult<?>> register(@RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(authService.register(registerDTO));
    }

    /**
     * 刷新令牌
     * @param request HTTP请求，用于获取当前令牌
     * @return 新的JWT令牌
     */
    @PostMapping("/refresh-token")
    @LogDetails
    public ResponseEntity<ApiResult<?>> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * 用户登出
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登出结果
     */
    @PostMapping("/logout")
    @LogDetails
    public ResponseEntity<ApiResult<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }
    
    /**
     * 重置密码请求
     * @param email 用户邮箱
     * @return 重置密码邮件发送结果
     */
    @PostMapping("/password-reset-request")
    @LogDetails
    public ResponseEntity<ApiResult<?>> passwordResetRequest(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendPasswordResetEmail(email));
    }
    
    /**
     * 重置密码
     * @param token 重置令牌
     * @param newPassword 新密码
     * @return 重置结果
     */
    @PostMapping("/password-reset")
    @LogDetails
    public ResponseEntity<ApiResult<?>> passwordReset(
            @RequestParam String token, 
            @RequestParam String newPassword) {
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }
}