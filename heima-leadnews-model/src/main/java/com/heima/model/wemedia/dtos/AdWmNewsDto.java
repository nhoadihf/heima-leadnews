package com.heima.model.wemedia.dtos;

import com.heima.model.wemedia.pojos.WmNews;
import lombok.Data;

@Data
public class AdWmNewsDto extends WmNews {

    private String authorName;
}
