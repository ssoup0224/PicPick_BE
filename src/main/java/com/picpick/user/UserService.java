package com.picpick.user;

import com.picpick.mart.Mart;
import com.picpick.mart.MartMapInfo;
import com.picpick.mart.MartMapper;
import com.picpick.mart.MartRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MartMapper martMapper;
    private final LoginLogRepository loginLogRepository;
    private final MartRepository martRepository;

    public UserLoginResult loginOrCreateUser(LoginRequest request, String ipAddress, String userAgent) {
        String uuid = request.getUuid();

        boolean isNewUser = userRepository.findByUuid(uuid).isEmpty();

        User user = userRepository.findByUuid(uuid).orElseGet(() -> {
            log.info("New user created: {}", uuid);
            UserDto userDto = UserDto.builder()
                    .uuid(uuid)
                    .createdAt(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .totalScans(0)
                    .build();
            return userMapper.toEntity(userDto);
        });
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        LoginLog loginLog = LoginLog.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        loginLogRepository.save(loginLog);
        return new UserLoginResult(userMapper.toDto(user), isNewUser);
    }

    public record UserLoginResult(UserDto user, boolean isNewUser) {
    }

    public MartMapInfo updateLocation(UpdateLocationRequest request) {
        Long userId = request.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setLongitude(request.getLongitude());
        user.setLatitude(request.getLatitude());

        // Find nearby marts (within 10km) and assign the closest one.
        List<Mart> nearbyMarts = martRepository.findNearbyMart(request.getLongitude(), request.getLatitude());
        if (!nearbyMarts.isEmpty()) {
            user.setMart(nearbyMarts.get(0));
        }

        userRepository.save(user);

        if (user.getMart() == null) {
            log.warn("No nearby marts found for user: {}", userId);
            return new MartMapInfo();
        }

        return martMapper.toMapInfo(user.getMart());
    }

    public UserDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return userMapper.toDto(user);
    }
}
