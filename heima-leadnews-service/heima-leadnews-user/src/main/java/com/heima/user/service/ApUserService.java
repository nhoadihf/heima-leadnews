package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDtos;
import com.heima.model.user.pojos.ApUser;

/**
 * @author moningxi
 * @date 2023/12/29
 */
public interface ApUserService extends IService<ApUser> {

    /**
     * app端登录功能
     */
    public ResponseResult login(LoginDtos dtos);
}
