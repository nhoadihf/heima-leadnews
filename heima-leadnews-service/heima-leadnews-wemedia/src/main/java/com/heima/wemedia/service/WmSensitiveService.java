package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.AdSensitiveSearchDto;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {

    /**
     * 平台后台分页查寻敏感词
     */
    ResponseResult listByDto(AdSensitiveSearchDto dto);

    /**
     * 编辑敏感词
     */
    ResponseResult updateName(WmSensitive wmSensitive);

    /**
     * 保存敏感词
     */
    ResponseResult saveSensitive(WmSensitive wmSensitive);

    /**
     * 删除敏感词
     */
    ResponseResult del(Integer id);
}
