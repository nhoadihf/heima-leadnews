package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.heima.common.constants.ArticleConstants.LOADTYPE_LOAD_MORE;
import static com.heima.common.constants.ArticleConstants.LOADTYPE_LOAD_NEW;

/**
 * @author moningxi
 * @date 2024/1/4
 */
@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {

    @Autowired
    private ApArticleService apArticleService;

    /**
     * 加载首页
     */
    @PostMapping("/load/")
    public ResponseResult load(@RequestBody ArticleHomeDto dto) {
        //return apArticleService.load(dto, LOADTYPE_LOAD_MORE);
        return apArticleService.load2(dto, LOADTYPE_LOAD_MORE, true);
    }

    /**
     * 加载更多
     */
    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(dto, LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载最新
     */
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(dto, LOADTYPE_LOAD_NEW);
    }
}
