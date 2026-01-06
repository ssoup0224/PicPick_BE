package com.picpick.controllers;

import com.picpick.dtos.LocationVerificationRequest;
import com.picpick.dtos.MartResponse;
import com.picpick.dtos.UserLoginRequest;
import com.picpick.dtos.UserResponse;

import com.picpick.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    // guest UUID 기반 로그인/회원 생성
    @PostMapping("/login")
    public ResponseEntity<?> guestLogin(@Valid @RequestBody UserLoginRequest request, HttpServletRequest httpRequest) {
        log.info("Guest login request: uuid={}", request.getUuid());

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        UserService.UserLoginResult result = userService.loginOrCreateGuest(request, ipAddress, userAgent);

        if (result.isNewUser()) {
            return ResponseEntity.status(201).body(result.getUserResponse());
        } else {
            return ResponseEntity.ok(java.util.Map.of("message", "login successful"));
        }
    }

    // UUID로 유저 조회
    @GetMapping("/{uuid}")
    public ResponseEntity<UserResponse> getUserByUuid(@PathVariable String uuid) {
        UserResponse response = userService.getUserByUuid(uuid);
        return ResponseEntity.ok(response);
    }

    // 위치 인증
//    @PostMapping("/location")
//    public ResponseEntity<MartResponse> verifyLocation(@RequestBody LocationVerificationRequest request) {
//        MartResponse response = userService.verifyLocation(request);
//        return ResponseEntity.ok(response);
//    }

    @PatchMapping("/location/update")
    public ResponseEntity<MartResponse> updateLocation(@RequestBody LocationVerificationRequest request) {
        MartResponse response = userService.updateLocation(request);
        return ResponseEntity.ok(response);
    }
}
