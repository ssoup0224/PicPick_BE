package com.picpick.services;

import com.picpick.dtos.UserLoginRequest;
import org.springframework.stereotype.Service;

import com.picpick.dtos.UserResponse;
import com.picpick.entities.Role;
import com.picpick.entities.User;
import com.picpick.mappers.UserMapper;
import com.picpick.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import com.picpick.dtos.LocationVerificationRequest;
import com.picpick.dtos.MartResponse;
import com.picpick.entities.LoginLog;
import com.picpick.entities.Mart;
import com.picpick.mappers.MartMapper;
import com.picpick.repositories.LoginLogRepository;
import com.picpick.repositories.MartRepository;
import org.springframework.data.domain.PageRequest;
import java.util.List;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MartRepository martRepository;
    private final MartMapper martMapper;

    // 위치 인증
    @Transactional
    public MartResponse verifyLocation(LocationVerificationRequest request) {
        String uuid = request.getUuid();
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("User not found: " + uuid));

        // 유저 위치 업데이트
        user.setCurrentLatitude(request.getLatitude());
        user.setCurrentLongitude(request.getLongitude());
        userRepository.save(user);

        // 가장 가까운 마트 찾기
        List<Mart> marts = martRepository.findNearestMart(
                request.getLatitude(),
                request.getLongitude(),
                PageRequest.of(0, 1));

        if (marts.isEmpty()) {
            throw new RuntimeException("No marts found near location");
        }

        return martMapper.toDto(marts.get(0));
    }

    private final LoginLogRepository loginLogRepository;

    // guest UUID로 사용자 조회 또는 생성 + lastLogin 갱신
    @Transactional
    public UserLoginResult loginOrCreateGuest(UserLoginRequest request, String ipAddress, String userAgent) {
        String uuid = request.getUuid();

        boolean isNewUser = userRepository.findByUuid(uuid).isEmpty();

        User user = userRepository.findByUuid(uuid)
                .orElseGet(() -> {
                    log.info("Creating new guest user with UUID: {}", uuid);
                    // Build UserResponse DTO first
                    UserResponse newUserDto = UserResponse.builder()
                            .uuid(uuid)
                            .role(Role.USER)
                            .totalScans(0)
                            .build();
                    // Convert DTO to entity using mapper
                    return userMapper.toEntity(newUserDto);
                });

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Login Log 저장
        LoginLog loginLog = LoginLog.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        loginLogRepository.save(loginLog);

        return new UserLoginResult(userMapper.toDto(user), isNewUser);
    }

    // 로그인 결과를 담는 내부 클래스
    public static class UserLoginResult {
        private final UserResponse userResponse;
        private final boolean isNewUser;

        public UserLoginResult(UserResponse userResponse, boolean isNewUser) {
            this.userResponse = userResponse;
            this.isNewUser = isNewUser;
        }

        public UserResponse getUserResponse() {
            return userResponse;
        }

        public boolean isNewUser() {
            return isNewUser;
        }
    }

    // UUID로 사용자 조회
    public UserResponse getUserByUuid(String uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("User not found with UUID: " + uuid));
        return toUserResponse(user);
    }

    // 스캔 횟수 증가 (ScanService에서 호출)
    public void incrementTotalScans(User user) {
        user.setTotalScans(user.getTotalScans() + 1);
        userRepository.save(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .totalScans(user.getTotalScans())
                .currentLongitude(user.getCurrentLongitude())
                .currentLatitude(user.getCurrentLatitude())
                .build();
    }
}
