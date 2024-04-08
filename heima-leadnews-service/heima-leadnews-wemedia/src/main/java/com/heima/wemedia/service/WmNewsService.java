package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdWmNewsAuthDto;
import com.heima.model.wemedia.dtos.AdWmNewsSearchDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import org.springframework.web.bind.annotation.RequestBody;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 查询文章列表
     */
    ResponseResult findList(WmNewsPageReqDto dto);

    /**
     * 发布文章修改与保存草稿
     */
    ResponseResult submitNews(WmNewsDto dto);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(@RequestBody WmNewsDto dto);

    /**
     * 平台后台分页查询文章
     */
    ResponseResult listByDto(AdWmNewsSearchDto dto);

    /**
     * 平台后台查看文章详情
     */
    ResponseResult showDetail(Integer id);

    /**
     * 审核成功
     */
    ResponseResult authPass(AdWmNewsAuthDto dto);

    /**
     * 审核失败
     */
    ResponseResult authFail(AdWmNewsAuthDto dto);
}
