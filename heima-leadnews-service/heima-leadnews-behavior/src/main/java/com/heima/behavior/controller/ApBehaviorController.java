package com.heima.behavior.controller;

import com.heima.behavior.service.ApBehaviorService;
import com.heima.model.behavior.dtos.BeLikesDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1")
public class ApBehaviorController {

    @Resource
    private ApBehaviorService apBehaviorService;

    @PostMapping("/likes_behavior")
    public ResponseResult likesBehavior(@RequestBody BeLikesDto dto) {
        return apBehaviorService.likesBehavior(dto);
    }

    @PostMapping("/un_likes_behavior")
    public ResponseResult unLikesBehavior(@RequestBody UnLikesBehaviorDto dto) {
        return apBehaviorService.unLikesBehavior(dto);
    }

    @PostMapping("/read_behavior")
    public ResponseResult readBehavior(@RequestBody ReadBehaviorDto dto){
        return apBehaviorService.readBehavior(dto);
    }
}
