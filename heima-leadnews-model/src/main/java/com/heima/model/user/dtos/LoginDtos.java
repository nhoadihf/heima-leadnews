package com.heima.model.user.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author moningxi
 * @date 2023/12/29
 */
@Data
public class LoginDtos {

    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String phone;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String password;
}
