package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ApUserAuthSearchDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUserRealname;

public interface ApUserAuthService extends IService<ApUserRealname> {

    /**
     * 分页查询用户审核列表
     */
    ResponseResult listByDto(ApUserAuthSearchDto dto);

    /**
     * 审核失败
     */
    ResponseResult authFail(ApUserAuthSearchDto dto);

    /**
     * 审核成功
     */
    ResponseResult authPass(ApUserAuthSearchDto dto);
}
