package com.green.spring_board.controller;


import com.green.spring_board.dto.*;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "그린 커뮤니티 회원 API", description = "회원 관련 API 모음입니다.")
@RestController
@RequestMapping("/api/user")
@AllArgsConstructor


public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입 API", description = "회원가입을 할 때 씀")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignupRequest signupRequest) {
        userService.signUp(signupRequest);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest loginRequest,
                                      HttpServletRequest httpServletRequest) {

        int userId = userService.login(loginRequest);
        HttpSession session = httpServletRequest.getSession();
        httpServletRequest.changeSessionId();
        session.setAttribute("userId", userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
    // 내 정보 조회하기 기능
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponse>> getCurrentUser(HttpServletRequest httpServletRequest) {
        // 이메일과 닉네임만 내려주기
        // 1. 이 사람의 세션을 가져옴
        /*
        /me는 회원 전용 서비스다.
        이사람의 세션이 없으면, 새로 만들어주는게 아니라 내쫒아야 함
        그래서 세션이 없으면 없다고 세션을 만들지 않도록 getSession 안에 (false) 옵션을 추가한다.
         */
        HttpSession session = httpServletRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        // 2. 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");
        MyInfoResponse response = userService.getUserInfo(userId);

        return ResponseEntity.ok().body(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 회원 정보 수정
    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(HttpServletRequest request,
                                               @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        HttpSession session = request.getSession(false);
        // 이메일 닉네임 업데이트
        // 현재 유저를 가져와서, 해당 유저 정보를 사용자가 올린 요청으로 덮어씌운다
        // 보드에서 구현했던것처럼  null 이면 수정하지 않기
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        int userId = (int) session.getAttribute("userId");
        userService.updateUserInfo(userId, userUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok());
        }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        // 1. DB 삭제
        userService.deleteUser(userId);
        // 2. 세션 비활성화
        session.invalidate();

        return ResponseEntity.ok(ApiResponse.ok());
    }
}


