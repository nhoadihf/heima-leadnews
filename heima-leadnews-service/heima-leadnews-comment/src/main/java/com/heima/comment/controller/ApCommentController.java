package com.heima.comment.controller;

import com.heima.comment.service.ApCommentService;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/comment")
public class ApCommentController {

    @Resource
    private ApCommentService apCommentService;

    @PostMapping("/save")
    public ResponseResult saveComment(@RequestBody CommentSaveDto dto) {
        return apCommentService.saveComment(dto);
    }

    @PostMapping("/load")
    public ResponseResult loadComm(@RequestBody CommentDto dto) {
        return apCommentService.loadComm(dto);
    }

    @PostMapping("/like")
    public ResponseResult likeComm(@RequestBody CommentLikeDto dto) {
        return apCommentService.liekComm(dto);
    }
}
