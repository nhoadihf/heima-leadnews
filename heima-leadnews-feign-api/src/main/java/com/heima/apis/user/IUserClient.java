package com.heima.apis.user;

import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "leadnews-user")
public interface IUserClient {

    @GetMapping("/api/v1/user/getById/{id}")
    ResponseResult getUser(@PathVariable("id") Integer id);
}
