package com.green.spring_board.entity;

import com.green.spring_board.dto.LoginRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "boards")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int hits;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedDatetime;

    // user(userId) -> profile(user_id)
    // 1:1 @OneToOne
    // board <-> user
    // n:1 , 1:n @OneToMany
    // n:m @ManyToMany
    // ManyToOne 기본값은 EAGER / FATCH는 명시해줘야됨.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int likeCount;

}