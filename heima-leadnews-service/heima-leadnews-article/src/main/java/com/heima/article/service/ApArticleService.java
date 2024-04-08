package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.dtos.ArticleUpdateDto;
import com.heima.model.article.mess.ArticleVisitStreamMess;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @author moningxi
 * @date 2024/1/4
 */
public interface ApArticleService extends IService<ApArticle> {

    /**
     * 加载首页
     */
    ResponseResult load(ArticleHomeDto dto, Short type);

    /**
     * 加载首页
     * firstPage 是否是首页 false不是 true 是
     */
    ResponseResult load2(ArticleHomeDto dto, Short type, boolean firstPage);

    /**
     * 保存app端的文章
     * @param dto
     * @return
     */
    ResponseResult saveArticle(ArticleDto dto);

    /**
     * 用户点击收藏
     */
    ResponseResult collectBehavior(CollectionBehaviorDto dto);

    /**
     * 加载文章行为
     */
    ResponseResult loadArticleBehavior(ArticleInfoDto dto);

    /**
     * 修改文章行为数据
     * type 1-likes  2-collection  3-comment  4-views
     */
    void updateArticleBehavior(ArticleUpdateDto dto);

    /**
     * 更新文章行为数据
     */
    void updateScore(ArticleVisitStreamMess mess);
}
