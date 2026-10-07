package com.green.spring_board.controller;

import com.green.spring_board.dto.*;
import com.green.spring_board.entity.Board;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor

public class BoardController {

    private final BoardService boardService;

    // 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoards(HttpServletRequest request){
//        return boardRepository.findAll(); <- 데이터만
        HttpSession session = request.getSession(false);

        int userId = -1;
        if (session != null && session.getAttribute("userId") != null) {
//            throw new UnauthenticatedException("로그인이 필요합니다.");
            userId = (int) session.getAttribute("userId");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(boardService.getAllBoards(userId))
        );
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardsDetail(@PathVariable int id,
                                                                      HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        int userId = -1;
        if (session != null && session.getAttribute("userId") != null) {
//            throw new UnauthenticatedException("로그인이 필요합니다.");
            userId = (int) session.getAttribute("userId");
        }

        BoardResponse board = boardService.getBoard(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(board));
    }

    // 내 게시글 조회
    @GetMapping("/me")
    // 게시글이 여러개일수있기 때문에 List로 받아야함
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getMyBoards(HttpServletRequest request) {
        // 기존 세션이 없으면 새로 만들지 말라는 옵션 (회원전용 기능)
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        List<BoardResponse> boards = boardService.getMyBoards(userId);

        return ResponseEntity.ok(ApiResponse.ok(boards));
    }

    // 삽입
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard
            (@Valid @RequestBody BoardCreateRequest boardCreateRequest,
            HttpServletRequest httpServletRequest) {

        HttpSession session = httpServletRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        // 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");
        int newBoardId = boardService.createBoard(boardCreateRequest, userId);
        URI location = URI.create("/api/board/" + newBoardId);

        return ResponseEntity.created(location).body(ApiResponse.ok());
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @Valid
            @PathVariable int id,
            @RequestBody BoardUpdateRequest boardUpdateRequest,
            HttpServletRequest httpServletRequest) {

        HttpSession session = httpServletRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        int userId = (int) session.getAttribute("userId");
        boardService.updateBoard(id, boardUpdateRequest, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
        ) {
            HttpSession session = httpServletRequest.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                throw new UnauthenticatedException("로그인이 필요합니다.");
            }


        // 삭제 성공 시 응답 방법 (둘 중 어느 방법을 쓸지는 속한 팀, 조직 컨벤션 따르기)
        // 1. 200 + ApiResponse<Void>
        // 2. 204 (No Content) + No Body
            int userId = (int) session.getAttribute("userId");
            boardService.deleteBoard(id, userId);
            return ResponseEntity.ok(ApiResponse.ok());
    }

    // 좋아요(Like)
    @PostMapping("/like/{id}")
    public ResponseEntity<ApiResponse<Void>> likeBoard(
            @PathVariable int id,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        boardService.pressLike(id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 상세 눌렀을 때 어느 유저들이 이 게시글 좋아요를 눌렀는지 (조회)
    @GetMapping("/like/{id}")
    public ResponseEntity<ApiResponse<LikeDetailResponse>> viewLikeDetails(
            @PathVariable int id,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        // 이 게시글에 좋아요 누른 유저들의 유저명
        LikeDetailResponse response = boardService.getLikeDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    // 내가 이 게시글 좋아요 눌렀는지
}
