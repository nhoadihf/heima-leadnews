package com.heima.comment.pojos.vos;

import com.heima.comment.pojos.ApComment;
import lombok.Data;

@Data
public class ApCommentList extends ApComment {
    /**
     * 是否点赞 0-点赞 1-没点赞
     */
    private Integer operation;

    /**
     * 评论人头像
     */
    private String authorImage;
}
