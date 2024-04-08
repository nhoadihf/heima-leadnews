package com.heima.model.user.dtos;

import lombok.Data;

@Data
public class UserRelationDto {
    private String articleId;

    private Integer authorId;

    /**
     * 0 关注 1 取消
     */
    private Short operation;
}
