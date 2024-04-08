package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdWmChannelSearchDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/channel")
public class WmChannelController {
    @Resource
    private WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult findAll() {
        return wmChannelService.findAll();
    }

    @PostMapping("/list")
    public ResponseResult listByDto(@RequestBody AdWmChannelSearchDto dto) {
        return wmChannelService.listByDto(dto);
    }

    @PostMapping("/save")
    public ResponseResult saveChannel(@RequestBody WmChannel wmChannel) {
        return wmChannelService.saveChannel(wmChannel);
    }

    @PostMapping("/update")
    public ResponseResult updateStatus(@RequestBody WmChannel wmChannel) {
        return wmChannelService.updateStatus(wmChannel);
    }
}
