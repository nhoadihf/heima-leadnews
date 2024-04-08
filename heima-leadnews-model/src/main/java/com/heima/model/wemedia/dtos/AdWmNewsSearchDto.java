package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class AdWmNewsSearchDto extends PageRequestDto {
    private String title;
    private Integer status;
}
