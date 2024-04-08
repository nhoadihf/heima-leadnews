package com.heima.user.service.impl;

import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.ApUserBehaviorService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.heima.common.constants.BehaviorConstants.USER_FOLLOW_AUTHOR;

@Service
@Slf4j
public class ApUserBehaviorServiceImpl implements ApUserBehaviorService {

    @Resource
    private CacheService cacheService;

    /**
     * 关注作者
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult userFollow(UserRelationDto dto) {
        if (dto.getAuthorId() == null || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = AppThreadLocalUtil.getWmUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        String key = USER_FOLLOW_AUTHOR + ":" + user.getId();
        boolean isFollow = cacheService.sIsMember(key, dto.getAuthorId().toString());
        if (isFollow) {
            cacheService.sRemove(key, dto.getAuthorId().toString());
        } else {
            cacheService.sAdd(key, String.valueOf(dto.getAuthorId()));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
