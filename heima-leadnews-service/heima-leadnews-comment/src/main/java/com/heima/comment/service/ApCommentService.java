package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCommentService {

    /**
     * 保存用户评论
     */
    ResponseResult saveComment(CommentSaveDto dto);

    /**
     * 加载文章评论
     */
    ResponseResult loadComm(CommentDto dto);

    /**
     * 评论点赞
     */
    ResponseResult liekComm(CommentLikeDto dto);
}
