package com.green.spring_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class BoardResponse {
    int id; // Board Id
    String title; // 제목
    String content; // 내용
    int hits; // 조회수
    Integer authorId; // 작성자 아이디
    String authorNickname; // 작성자 닉네임
    LocalDateTime createDatetime; // 생성일시
    LocalDateTime updateDatetime; // 수정일시
}
