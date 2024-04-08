package com.heima.user.controller.v1;

import com.heima.model.admin.dtos.ApUserAuthSearchDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.ApUserAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserAuthController {

    @Resource
    private ApUserAuthService apUserAuthService;

    @PostMapping("/list")
    public ResponseResult listByDto(@RequestBody ApUserAuthSearchDto dto) {
        return apUserAuthService.listByDto(dto);
    }

    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody ApUserAuthSearchDto dto) {
        return apUserAuthService.authFail(dto);
    }

    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody ApUserAuthSearchDto dto) {
        return apUserAuthService.authPass(dto);
    }
}
