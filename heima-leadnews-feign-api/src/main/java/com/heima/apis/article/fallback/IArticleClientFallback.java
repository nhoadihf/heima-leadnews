package com.heima.apis.article.fallback;

import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleUpdateDto;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IArticleClientFallback implements IArticleClient {
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        // 一直超时 直接返回200
        log.error("远程调用超时---IArticleClientFallback");
        return ResponseResult.okResult(dto.getId());
    }

    /**
     * 修改文章行为数据
     * type 1-likes  2-collection  3-comment  4-views
     */
    @Override
    public void updateArticleBehavior(ArticleUpdateDto dto) {

    }

    /**
     * 根据文章id查询文章
     *
     * @param id
     */
    @Override
    public ResponseResult showById(Long id) {
        return ResponseResult.okResult(id);
    }
}
