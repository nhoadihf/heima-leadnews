package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author moningxi
 * @date 2024/1/4
 */
@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    /**
     * @param dto
     * @param type 1--加载更多   2--加载最新
     * @return
     */
    List<ApArticle> loadArticleList(ArticleHomeDto dto, Short type);

    /**
     * 查询前5天的文章集合
     * @param dateParam
     * @return
     */
    List<ApArticle> findArticleListByLast5days(@Param("dayParam") Date dateParam);
}
