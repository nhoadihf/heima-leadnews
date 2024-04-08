package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.AdSensitiveSearchDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive> implements WmSensitiveService {
    /**
     * 平台后台分页查寻敏感词
     *
     * @param dto
     */
    @Override
    public ResponseResult listByDto(AdSensitiveSearchDto dto) {
        dto.checkParam();
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmSensitive> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(dto.getName())) {
            lambdaQueryWrapper.like(WmSensitive::getSensitives, dto.getName());
        }
        lambdaQueryWrapper.orderByDesc(WmSensitive::getCreatedTime);
        page = page(page, lambdaQueryWrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 编辑敏感词
     * @param wmSensitive
     */
    @Override
    public ResponseResult updateName(WmSensitive wmSensitive) {
        if (wmSensitive.getId() == null || StringUtils.isBlank(wmSensitive.getSensitives())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmSensitive sensitive = getById(wmSensitive.getId());
        if (sensitive == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "敏感词不存在");
        }
        sensitive.setSensitives(wmSensitive.getSensitives());
        updateById(sensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 保存敏感词
     *
     * @param wmSensitive
     */
    @Override
    public ResponseResult saveSensitive(WmSensitive wmSensitive) {
        if (StringUtils.isBlank(wmSensitive.getSensitives())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "敏感词不能为空");
        }

        wmSensitive.setCreatedTime(new Date());
        save(wmSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除敏感词
     *
     * @param id
     */
    @Override
    public ResponseResult del(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
