package com.heima.model.behavior.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class CollectionBehaviorDto {
    /**
     * 文章id
     */
    private String entryId;

    /**
     * 0收藏 1取消收藏
     */
    private Short operation;

    /**
     * 发布时间
     */
    private Date publishedTime;

    /**
     * 0文章 1动态
     */
    private Short type;
}
