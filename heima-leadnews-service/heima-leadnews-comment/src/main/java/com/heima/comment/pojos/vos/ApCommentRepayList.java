package com.heima.comment.pojos.vos;

import com.heima.comment.pojos.ApCommentRepay;
import lombok.Data;

@Data
public class ApCommentRepayList extends ApCommentRepay {
    private Integer operation;

    /**
     * 作者头像
     */
    private String authorImage;
}
