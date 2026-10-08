package com.green.spring_board.controller;

import com.green.spring_board.dto.ApiResponse;
import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.dto.CommentUpdateRequest;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor

public class CommentController {
    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("board/{boardId}/comments")
    public ResponseEntity<ApiResponse<Void>> createComment
                                                (@PathVariable int boardId, @Valid
                                                 @RequestBody CommentCreateRequest commentCreateRequest,
                                                 HttpServletRequest  httpServletRequest) {

        HttpSession session = httpServletRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        int userId = (int) session.getAttribute("userId");
        commentService.createComment(commentCreateRequest, userId, boardId);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 댓글 조회
    @GetMapping("board/{boardId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> readComments(@PathVariable int boardId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.readComments(boardId)));
    }

    // 댓글 수정
    @PatchMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> updateComment(@PathVariable int id,
                                                           @RequestBody CommentUpdateRequest commentUpdateRequest,
                                                           @Valid HttpServletRequest httpServletRequest) {
        // 코드 미완성
        HttpSession session = httpServletRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        int userId = (int) session.getAttribute("userId");
        commentService.updateComment(commentUpdateRequest, id, userId);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 댓글 삭제
    @DeleteMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ){
        HttpSession session = httpServletRequest.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.deleteComment(id, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok());
    }
}
