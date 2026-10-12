package com.green.spring_board.service;

import com.green.spring_board.dto.*;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.AuthorizationFailureException;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.global.UserState;
import com.green.spring_board.repository.UserRepository;
import com.green.spring_board.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public void signUp(SignupRequest signupRequest) {

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResourceConflictException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new ResourceConflictException("이미 존재하는 아이디입니다.");
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // DB save
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setNickname(signupRequest.getNickname());
        user.setState(UserState.ACTIVE);
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword()
            ));
        } catch (AuthenticationException e) {
            throw new UnauthenticatedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        // 1. 이메일이 존재하는건지 확인
        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (user == null) {
            throw new ResourceNotFoundException("회원정보를 찾을 수 없습니다.");
        }

        if(user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

        // 액세스 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());

        // 3. 로그인 성공 -> 컨트롤러
        return new LoginResponse(accessToken);
    }

    public MyInfoResponse getUserInfo(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("회원정보를 찾을 수 없습니다.");
        }

        // 3. 유저 아이디로 DB 조회함
        User user = userOptional.get();

        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

        // 4. DB에서 이 유저의 닉네임과 이메일을 받아옴
        String email = user.getEmail();
        String nickname = user.getNickname();

        // 5. 돌려줌
        MyInfoResponse myInfoResponse = new MyInfoResponse();
        myInfoResponse.setEmail(email);
        myInfoResponse.setNickname(nickname);

        return myInfoResponse;
    }

    public MyInfoResponse updateUserInfo(int userId, UserUpdateRequest myInfoResponse) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("회원정보를 찾을 수 없습니다.");
        }

        User user = userOptional.get();

        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(myInfoResponse.getEmail())) {
            throw new ResourceConflictException("이미 존재하는 이메일입니다.");
        }

        if (myInfoResponse.getEmail() != null && !myInfoResponse.getEmail().isBlank()) {
            user.setEmail(myInfoResponse.getEmail());
        }
        if (myInfoResponse.getNickname() != null && !myInfoResponse.getNickname().isBlank()) {
            user.setNickname(myInfoResponse.getNickname());
        }
        userRepository.save(user);
        return getUserInfo(userId);
    }

    public void deleteUser(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("회원정보를 찾을 수 없습니다.");
        }
        User user = userOptional.get();

        user.setState(UserState.QUITTED);
        userRepository.save(user);
    }
}
