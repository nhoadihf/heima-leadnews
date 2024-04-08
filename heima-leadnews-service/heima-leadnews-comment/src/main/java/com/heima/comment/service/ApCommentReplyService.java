package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentRepayDto;
import com.heima.model.comment.dtos.CommentRepayLikeDto;
import com.heima.model.comment.dtos.CommentRepaySaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCommentReplyService {
    /**
     * 加载评论回复
     */
    ResponseResult loadList(CommentRepayDto dto);

    /**
     * 回复评论
     */
    ResponseResult saveComm(CommentRepaySaveDto dto);

    /**
     * 评论回复点赞
     */
    ResponseResult likeRepay(CommentRepayLikeDto dto);
}
