package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDtos;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author moningxi
 * @date 2023/12/29
 */
@RestController
@RequestMapping("/api/v1/login")
@Api(value = "app端用户登录", tags = "app端用户登录")
public class ApUserLoginController {

    @Autowired
    ApUserService apUserService;

    @PostMapping("/login_auth")
    @ApiOperation(value = "用户登录")
    public ResponseResult login(@RequestBody LoginDtos dto) {
        return apUserService.login(dto);
    }
}