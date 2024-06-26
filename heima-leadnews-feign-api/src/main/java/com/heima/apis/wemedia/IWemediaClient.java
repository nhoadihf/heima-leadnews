package com.heima.apis.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "leadnews-wemedia")
public interface IWemediaClient {

    @GetMapping("/api/v1/channel/list")
    ResponseResult getChannels();
}
