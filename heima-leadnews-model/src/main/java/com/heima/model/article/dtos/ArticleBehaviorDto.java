package com.heima.model.article.dtos;

import lombok.Data;

@Data
public class ArticleBehaviorDto {

    private Boolean islike;
    private Boolean isunlike;
    private Boolean iscollection;
    private Boolean isfollow;
}
