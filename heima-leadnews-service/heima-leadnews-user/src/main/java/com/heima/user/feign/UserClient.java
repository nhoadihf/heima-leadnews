package com.heima.user.feign;

import com.heima.apis.user.IUserClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.ApUserService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserClient implements IUserClient {
    @Resource
    private ApUserService apUserService;

    /**
     * @param id
     * @return
     */
    @Override
    public ResponseResult getUser(Integer id) {
        ApUser user = apUserService.getById(id);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "用户不存在");
        }
        return ResponseResult.okResult(user);
    }
}
