package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.AdWmChannelSearchDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {
    /**
     * 查询所有频道列表
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    /**
     * 后台分页查询频道列表
     * @param dto
     */
    @Override
    public ResponseResult listByDto(AdWmChannelSearchDto dto) {
        // 校验参数
        dto.checkParam();

        //分页查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmChannel> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(dto.getName())) {
            lambdaQueryWrapper.like(WmChannel::getName, dto.getName());
        }
        // 倒叙排序
        lambdaQueryWrapper.orderByAsc(WmChannel::getOrd).orderByDesc(WmChannel::getCreatedTime);
        page = page(page, lambdaQueryWrapper);

        // 处理结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 保存频道
     *
     * @param wmChannel
     */
    @Override
    public ResponseResult saveChannel(WmChannel wmChannel) {
        if (StringUtils.isBlank(wmChannel.getName())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "频道名称不能为空");
        }
        wmChannel.setIsDefault(true);
        wmChannel.setCreatedTime(new Date());
        save(wmChannel);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 启用或禁用频道
     *
     * @param wmChannel
     */
    @Override
    public ResponseResult updateStatus(WmChannel wmChannel) {
        if (wmChannel.getId() == null || wmChannel.getStatus() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 查询
        WmChannel dbChannel = getById(wmChannel.getId());
        if (dbChannel == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "频道不存在");
        }

        dbChannel.setStatus(wmChannel.getStatus());
        updateById(dbChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
