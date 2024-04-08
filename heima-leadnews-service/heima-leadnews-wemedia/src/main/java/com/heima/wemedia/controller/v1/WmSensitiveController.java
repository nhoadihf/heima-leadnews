package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdSensitiveSearchDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/sensitive")
public class WmSensitiveController {

    @Resource
    private WmSensitiveService wmSensitiveService;

    @PostMapping("/list")
    public ResponseResult listByDto(@RequestBody AdSensitiveSearchDto dto) {
        return wmSensitiveService.listByDto(dto);
    }

    @PostMapping("/update")
    public ResponseResult updateName(@RequestBody WmSensitive wmSensitive) {
        return wmSensitiveService.updateName(wmSensitive);
    }

    @PostMapping("/save")
    public ResponseResult saveSensitive(@RequestBody WmSensitive wmSensitive) {
        return wmSensitiveService.saveSensitive(wmSensitive);
    }

    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable("id") Integer id) {
        return wmSensitiveService.del(id);
    }
}
