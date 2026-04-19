package com.careerfit.backend.domain.auth.service;

import com.careerfit.backend.common.exception.CustomException;
import com.careerfit.backend.common.exception.ErrorCode;
import com.careerfit.backend.common.jwt.JwtProvider;
import com.careerfit.backend.domain.auth.dto.LoginRequest;
import com.careerfit.backend.domain.auth.dto.RegisterRequest;
import com.careerfit.backend.domain.auth.dto.TokenResponse;
import com.careerfit.backend.domain.auth.mapper.AuthMapper;
import com.careerfit.backend.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public void register(RegisterRequest request) {
        log.info("[AuthService] 회원가입 요청 - email: {}", request.getEmail());

        // 이메일 중복 확인
        if (authMapper.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 일치 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Users 객체 생성
        Users user = Users.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .role("USER")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // DB 저장
        authMapper.save(user);
        log.info("[AuthService] 회원가입 완료 - email: {}", request.getEmail());
    }

    public TokenResponse login(LoginRequest request) {
        log.info("[AuthService] 로그인 요청 - email: {}", request.getEmail());

        // 이메일로 사용자 조회
        Users user = authMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new com.careerfit.backend.common.jwt.CustomUserDetails(
                        user.getId(), user.getEmail(), user.getPassword(), user.getRole()
                ),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );

        // 토큰 생성
        String accessToken  = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        log.info("[AuthService] 로그인 완료 - email: {}", request.getEmail());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}