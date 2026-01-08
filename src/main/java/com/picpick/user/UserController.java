package com.picpick.user;

import com.picpick.mart.MartMapInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("User login request: {}", request);

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        UserService.UserLoginResult result = userService.loginOrCreateUser(request, ipAddress, userAgent);

        if (result.isNewUser()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "새로운 유저 생성되었습니다.", "userId", result.user().getId()));
        } else {
            return ResponseEntity.ok(Map.of("message", "로그인 성공하였습니다.", "userId", result.user().getId()));
        }
    }

    @PatchMapping("/update-location")
    public ResponseEntity<MartMapInfo> updateLocation(@RequestBody UpdateLocationRequest request) {
        MartMapInfo response = userService.updateLocation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserInfo(@PathVariable Long userId) {
        UserDto response = userService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }
}
