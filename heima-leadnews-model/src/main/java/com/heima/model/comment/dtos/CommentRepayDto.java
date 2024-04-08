package com.heima.model.comment.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class CommentRepayDto {
    private String commentId;
    private Date minDate;
    private Integer size;
}
