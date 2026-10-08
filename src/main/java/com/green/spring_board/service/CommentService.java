package com.green.spring_board.service;

import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.dto.CommentUpdateRequest;
import com.green.spring_board.entity.Board;
import com.green.spring_board.entity.Comment;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.AuthorizationFailureException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.repository.CommentRepository;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    // 댓글 생성
    public void createComment (CommentCreateRequest commentCreateRequest, int userId, int boardId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Board> boardOptional = boardRepository.findById(boardId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("사용자를 찾을 수 없습니다.");
        }

        if (boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        User user = userOptional.get();
        Board board = boardOptional.get();

        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        comment.setBoard(board);
        comment.setUser(user);
        commentRepository.save(comment);
    }

    // 댓글 조회
    public List<CommentResponse> readComments (int boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("게시글이 존재하지 않습니다.");
        }

        List<Comment> comments = commentRepository.findByBoardIdAndIsDeletedFalse(boardId);
        // 댓글은 가져왔는데 이걸 이제 CommentResponse 로 변환

        List<CommentResponse> commentResponses = new ArrayList<>();
        for (Comment comment : comments) {
            CommentResponse commentResponse = new CommentResponse();
            commentResponse.setCommentId(comment.getId());
            commentResponse.setContent(comment.getContent());
            commentResponse.setNickname(comment.getUser().getNickname());
            commentResponse.setCommentDate(comment.getCreatedDatetime());

            commentResponses.add(commentResponse);
        }
        return commentResponses;
    }

    // 댓글 수정
    public void updateComment (CommentUpdateRequest commentUpdateRequest, int commentId, int userId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시글이 존재하지 않습니다.");
        }
        Comment comment = commentOptional.get();

        if (comment.isDeleted())
            throw new ResourceNotFoundException("삭제된 댓글입니다.");

        if (comment.getUser().getId() != userId) {
            throw new AuthorizationFailureException("수정할 권한이 없습니다.");
        }

        if(commentUpdateRequest.getContent() != null){
            comment.setContent(commentUpdateRequest.getContent());
            commentRepository.save(comment);
        }
    }

    public void deleteComment (int commentId, int userId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시글이 존재하지 않습니다.");
        }
        Comment comment = commentOptional.get();

        if (comment.getUser().getId() != userId) {
            throw new AuthorizationFailureException("삭제할 권한이 없습니다.");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
