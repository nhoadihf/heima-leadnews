package com.heima.comment.controller;

import com.heima.comment.service.ApCommentReplyService;
import com.heima.model.comment.dtos.CommentRepayDto;
import com.heima.model.comment.dtos.CommentRepayLikeDto;
import com.heima.model.comment.dtos.CommentRepaySaveDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/comment_repay")
public class ApCommentReplyController {

    @Resource
    private ApCommentReplyService apCommentReplyService;

    @PostMapping("/load")
    public ResponseResult loadList(@RequestBody CommentRepayDto dto) {
        return apCommentReplyService.loadList(dto);
    }

    @PostMapping("/save")
    public ResponseResult saveComm(@RequestBody CommentRepaySaveDto dto) {
        return apCommentReplyService.saveComm(dto);
    }

    @PostMapping("/like")
    public ResponseResult likeRepay(@RequestBody CommentRepayLikeDto dto) {
        return apCommentReplyService.likeRepay(dto);
    }
}
