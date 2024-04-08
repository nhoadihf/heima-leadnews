package com.heima.apis.article;

import com.heima.apis.article.fallback.IArticleClientFallback;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleUpdateDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "leadnews-article", url = "http://localhost:51802", fallback = IArticleClientFallback.class)
public interface IArticleClient {

    /**
     * 保存app端的文章
     *
     * @param dto
     * @return
     */
    @PostMapping("/api/v1/article/save")
    ResponseResult saveArticle(@RequestBody ArticleDto dto);

    /**
     * 修改文章行为数据
     * type 1-likes  2-collection  3-comment  4-views
     */
    @PostMapping("/api/v1/article/update_behavior")
    void updateArticleBehavior(@RequestBody ArticleUpdateDto dto);

    /**
     * 根据文章id查询文章
     */
    @GetMapping("/api/v1/article/showById/{id}")
    ResponseResult showById(@PathVariable("id") Long id);
}
