package com.heima.model.comment.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class CommentDto {
    private Long articleId;
    private Date minDate;
    private Integer index;
}
