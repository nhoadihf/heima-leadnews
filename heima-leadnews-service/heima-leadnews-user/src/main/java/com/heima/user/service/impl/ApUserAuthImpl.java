package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.ApUserAuthConstants;
import com.heima.model.admin.dtos.ApUserAuthSearchDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.mapper.ApUserAuthMapper;
import com.heima.user.service.ApUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class ApUserAuthImpl extends ServiceImpl<ApUserAuthMapper, ApUserRealname> implements ApUserAuthService {

    /**
     * 分页查询用户审核列表
     *
     * @param dto
     */
    @Override
    public ResponseResult listByDto(ApUserAuthSearchDto dto) {
        // 校验参数
        dto.checkParam();
        // 分页查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ApUserRealname> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(ApUserRealname::getStatus, dto.getStatus());
        }
        // 倒叙排序
        lambdaQueryWrapper.orderByDesc(ApUserRealname::getCreatedTime);
        page = page(page, lambdaQueryWrapper);
        // 处理结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 审核失败
     *
     * @param dto
     */
    @Override
    public ResponseResult authFail(ApUserAuthSearchDto dto) {
        // 校验参数
        if (dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 查询
        ApUserRealname apUserRealname = getById(dto.getId());
        if (apUserRealname == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if (!apUserRealname.getStatus().equals(ApUserAuthConstants.REVIEWING_STATUS)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH, "当前用户状态不为待审核");
        }

        // 修改状态
        apUserRealname.setReason(dto.getMsg());
        apUserRealname.setStatus(ApUserAuthConstants.REVIEWED_FAIL_STATUS);
        apUserRealname.setUpdatedTime(new Date());
        updateById(apUserRealname);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 审核成功
     *
     * @param dto
     */
    @Override
    public ResponseResult authPass(ApUserAuthSearchDto dto) {
        // 校验参数
        if (dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 查询
        ApUserRealname apUserRealname = getById(dto.getId());
        if (apUserRealname == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if (!apUserRealname.getStatus().equals(ApUserAuthConstants.REVIEWING_STATUS)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH, "当前用户状态不为待审核");
        }

        // 修改状态
        apUserRealname.setStatus(ApUserAuthConstants.REVIEWED_SUCCESS_STATUS);
        apUserRealname.setUpdatedTime(new Date());
        updateById(apUserRealname);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
