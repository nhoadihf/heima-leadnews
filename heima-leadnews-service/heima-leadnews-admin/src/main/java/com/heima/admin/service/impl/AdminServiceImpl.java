package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdminMapper;
import com.heima.admin.service.AdminService;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class AdminServiceImpl extends ServiceImpl<AdminMapper, AdUser> implements AdminService {

    /**
     * 管理员登录
     *
     * @param adUser
     * @return
     */
    @Override
    public ResponseResult login(AdUser adUser) {
        // 效验参数
        if (StringUtils.isBlank(adUser.getName()) || StringUtils.isBlank(adUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        AdUser dbUser = getOne(Wrappers.<AdUser>lambdaQuery().eq(AdUser::getName, adUser.getName()));
        String salt = dbUser.getSalt();
        String loginPassword = adUser.getPassword();
        String pass = DigestUtils.md5DigestAsHex((loginPassword + salt).getBytes());
        if (!pass.equals(dbUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }

        String token = AppJwtUtil.getToken(dbUser.getId().longValue());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        dbUser.setPassword("");
        dbUser.setSalt("");
        map.put("user", dbUser);
        return ResponseResult.okResult(map);
    }
}
