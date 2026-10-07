package com.green.spring_board.dto;

import com.green.spring_board.entity.Like;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class LikeDetailResponse {
    private List<String> LikedUserNames;
}
