package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdWmChannelSearchDto;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道列表
     */
    ResponseResult findAll();

    /**
     * 后台分页查询频道列表
     */
    ResponseResult listByDto(AdWmChannelSearchDto dto);

    /**
     * 保存频道
     */
    ResponseResult saveChannel(WmChannel wmChannel);

    /**
     * 启用或禁用频道
     */
    ResponseResult updateStatus(WmChannel wmChannel);
}
