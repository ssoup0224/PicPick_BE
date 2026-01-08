package com.picpick.mart;

import com.picpick.user.UpdateLocationRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/mart")
public class MartController {
    private final MartService martService;

    @PostMapping("/register")
    public ResponseEntity<?> registerMart(@Valid @RequestBody SignupRequest request) {
        log.info("Mart signup request: {}", request);
        Long martId = martService.registerMart(request);
        return ResponseEntity.created(URI.create("/mart/register"))
                .body((Map.of("message", "마트 등록 성공하였습니다.", "martId", martId)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> martLogin(@Valid @RequestBody MartLoginRequest request) {
        log.info("Mart login request with registration number: {}", request.getRegistrationNumber());
        MartDto mart = martService.martLogin(request);
        return ResponseEntity.ok(Map.of("message", "로그인 성공하였습니다.", "martId", mart));
    }

    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadExcelFile(@RequestPart MultipartFile file, @RequestParam Long martId) {
        martService.uploadExcelFile(file, martId);
        return ResponseEntity.created(URI.create("/mart/upload-file"))
                .body(Map.of("message", "파일 업로드 성공하였습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUploadedFile(@PathVariable Long id) {
        martService.deleteUploadedFile(id);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MartDto> viewMartInfo(@PathVariable Long id) {
        var mart = martService.viewMartInfo(id);
        return ResponseEntity.ok(mart);
    }

    @PatchMapping("/update-location")
    public ResponseEntity<?> updateLocation(@RequestBody UpdateLocationRequest request) {
        martService.updateLocation(request);
        return ResponseEntity.ok(Map.of("message", "위치 정보가 업데이트되었습니다."));
    }

    @DeleteMapping("/{id}/file")
    public ResponseEntity<?> deleteUploadedFileFromMart(@PathVariable Long id) {
        martService.deleteUploadedFileFromMart(id);
        return ResponseEntity.ok(Map.of("message", "파일이 삭제되었습니다."));
    }

}
