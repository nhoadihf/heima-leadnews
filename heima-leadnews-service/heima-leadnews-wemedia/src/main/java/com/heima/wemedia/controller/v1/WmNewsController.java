package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdWmNewsAuthDto;
import com.heima.model.wemedia.dtos.AdWmNewsSearchDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Resource
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmNewsPageReqDto dto) {
        return wmNewsService.findList(dto);
    }

    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto) {
        return wmNewsService.submitNews(dto);
    }

    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto) {
        return wmNewsService.downOrUp(dto);
    }

    @PostMapping("/list_vo")
    public ResponseResult listByDto(@RequestBody AdWmNewsSearchDto dto) {
        return wmNewsService.listByDto(dto);
    }

    @GetMapping("/one_vo/{id}")
    public ResponseResult showDetail(@PathVariable("id") Integer id) {
        return wmNewsService.showDetail(id);
    }

    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody AdWmNewsAuthDto dto) {
        return wmNewsService.authPass(dto);
    }

    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody AdWmNewsAuthDto dto) {
        return wmNewsService.authFail(dto);
    }
}
