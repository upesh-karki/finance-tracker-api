package com.upkdev.financialtracker.domain.member;

import com.upkdev.financialtracker.domain.member.dto.LoginRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberResponse;
import com.upkdev.financialtracker.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberResponse>> login(@Valid @RequestBody LoginRequest request) {
        MemberResponse response = memberService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Member registered successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(@PathVariable Long id) {
        MemberResponse response = memberService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponse>>> findAll() {
        List<MemberResponse> members = memberService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(members));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        memberService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Member deleted", null));
    }
}
