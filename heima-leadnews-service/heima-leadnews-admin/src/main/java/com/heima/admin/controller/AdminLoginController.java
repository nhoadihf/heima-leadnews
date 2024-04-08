package com.heima.admin.controller;

import com.heima.admin.service.AdminService;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class AdminLoginController {

    @Resource
    private AdminService adminService;

    @PostMapping("/login/in")
    public ResponseResult login(@RequestBody AdUser adUser) {
        return adminService.login(adUser);
    }
}
