package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;

public interface ApUserBehaviorService {

    /**
     * 关注作者
     * @param dto
     * @return
     */
    ResponseResult userFollow(UserRelationDto dto);
}
