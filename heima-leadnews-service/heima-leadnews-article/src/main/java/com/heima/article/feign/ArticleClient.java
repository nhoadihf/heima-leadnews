package com.heima.article.feign;

import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleUpdateDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class ArticleClient implements IArticleClient {

    @Resource
    private ApArticleService apArticleService;

    /**
     * 保存app端的文章
     *
     * @param dto
     * @return
     */
    @PostMapping("/api/v1/article/save")
    @Override
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) {
        return apArticleService.saveArticle(dto);
    }

    /**
     * 修改文章行为数据
     * type 1-likes  2-collection  3-comment  4-views
     */
    @Override
    @PostMapping("/api/v1/article/update_behavior")
    public void updateArticleBehavior(@RequestBody ArticleUpdateDto dto) {
        apArticleService.updateArticleBehavior(dto);
    }

    /**
     * 根据文章id查询文章
     *
     * @param id
     */
    @Override
    @GetMapping("/api/v1/article/showById/{id}")
    public ResponseResult showById(@PathVariable("id") Long id) {
        ApArticle article = apArticleService.getById(id);
        if (article != null) {
            return ResponseResult.okResult(article);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章不存在");
    }
}
