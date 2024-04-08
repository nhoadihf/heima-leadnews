package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;

public interface AdminService extends IService<AdUser> {

    /**
     * 管理员登录
     * @param adUser
     * @return
     */
    ResponseResult login(AdUser adUser);
}
