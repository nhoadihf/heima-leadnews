package com.heima.behavior.service;

import com.heima.model.behavior.dtos.BeLikesDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApBehaviorService {

    /**
     * 点赞
     */
    ResponseResult likesBehavior(BeLikesDto dto);

    /**
     * 文章不喜欢
     */
    ResponseResult unLikesBehavior(UnLikesBehaviorDto dto);

    /**
     * 修改阅读次数
     * @param dto
     * @return
     */
    ResponseResult readBehavior(ReadBehaviorDto dto);
}
