package com.green.spring_board.service;

import com.green.spring_board.dto.LoginRequest;
import com.green.spring_board.dto.MyInfoResponse;
import com.green.spring_board.dto.SignupRequest;
import com.green.spring_board.dto.UserUpdateRequest;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void signUp(SignupRequest signupRequest) {

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // DB save
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setNickname(signupRequest.getNickname());
        userRepository.save(user);
    }

    public int login(LoginRequest loginRequest) {
        // 1. 이메일이 존재하는건지 확인
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get(); // 이 이메일의 사용자 정보

        // 2. 비밀번호가 올바른지 확인
//        loginRequest.getPassword() // 사용자가 주장하는 비밀번호
//        user.getPassword() // DB에 저장된 비밀번호

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthenticatedException("Wrong password");
        }

        // 3. 로그인 성공 -> 컨트롤러
        return user.getId();
    }

    public MyInfoResponse getUserInfo(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        // 3. 유저 아이디로 DB 조회함
        User user = userOptional.get();

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
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get();

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(myInfoResponse.getEmail())) {
            throw new ResourceConflictException("Email already exists");
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
            throw new ResourceNotFoundException("User not found");
        }
        User user = userOptional.get();
        userRepository.delete(user);
    }
}
