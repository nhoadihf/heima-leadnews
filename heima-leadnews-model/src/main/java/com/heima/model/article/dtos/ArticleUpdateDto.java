package com.heima.model.article.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateDto {
    private String articleId;
    private Integer type;
    private Integer num;
}
