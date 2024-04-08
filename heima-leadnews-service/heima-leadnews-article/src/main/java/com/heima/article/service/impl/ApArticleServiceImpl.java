package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.*;
import com.heima.model.article.mess.ArticleVisitStreamMess;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.heima.common.constants.ArticleConstants.*;
import static com.heima.common.constants.BehaviorConstants.*;

/**
 * @author moningxi
 * @date 2024/1/4
 */
@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;
    private final static Short PAGE_MAX_SIZE = 50;
    @Resource
    private CacheService cacheService;

    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        // 校验参数
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        size = Math.min(size, PAGE_MAX_SIZE);
        dto.setSize(size);

        if (!type.equals(LOADTYPE_LOAD_MORE) && !type.equals(LOADTYPE_LOAD_NEW)) {
            type = LOADTYPE_LOAD_MORE;
        }

        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(DEFAULT_TAG);
        }

        if (dto.getMaxBehotTime() == null) {
            dto.setMaxBehotTime(new Date());
        }

        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }
        return ResponseResult.okResult(apArticleMapper.loadArticleList(dto, type));
    }

    @Resource
    private ApArticleConfigMapper apArticleConfigMapper;

    @Resource
    private ApArticleContentMapper apArticleContentMapper;
    @Resource
    private ArticleFreemarkerService articleFreemarkerService;

    /**
     * 保存app端的文章
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        // 校验参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);

        // 是否存在id
        if (dto.getId() == null) {
            // 不存在
            // 保存文章
            save(apArticle);
            // 保存文章配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);
            // 保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        } else {
            // 存在修改文章内容
            updateById(apArticle);

            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, apArticle.getId()));
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }
        // 异步调用生成模板文件
        articleFreemarkerService.buildArticleToMinIO(apArticle, dto.getContent());
        // 返回文章id
        return ResponseResult.okResult(apArticle.getId());
    }

    /**
     * 用户点击收藏
     *
     * @param dto
     */
    @Override
    public ResponseResult collectBehavior(CollectionBehaviorDto dto) {
        if (StringUtils.isBlank(dto.getEntryId()) || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = AppThreadLocalUtil.getWmUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        String key = APARTICLE_CONTENT_COLLECT + ":" + dto.getEntryId() + ":" + user.getId();
        boolean exists = cacheService.hExists(key, BEHAVIOR_COLLECT);
        if (!exists) {
            ArticleUpdateDto updateDto = new ArticleUpdateDto(dto.getEntryId(), 2, 1);
            updateArticleBehavior(updateDto);
        } else {
            if (dto.getOperation().equals(0)) {
                ArticleUpdateDto updateDto = new ArticleUpdateDto(dto.getEntryId(), 2, 1);
                updateArticleBehavior(updateDto);
            } else {
                ArticleUpdateDto updateDto = new ArticleUpdateDto(dto.getEntryId(), 2, -1);
                updateArticleBehavior(updateDto);
            }
        }
        cacheService.hPut(key, BEHAVIOR_COLLECT, String.valueOf(dto.getOperation()));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 加载文章行为
     *
     * @param dto
     */
    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto) {
        ArticleBehaviorDto result = new ArticleBehaviorDto();

        ApUser user = AppThreadLocalUtil.getWmUser();
        // 查寻redis
        String suffixKey = ":" + dto.getArticleId() + ":" + user.getId();
        // 是否点赞
        String reLike = (String) cacheService.hGet(APARTICLE_CONTENT_LIKE + suffixKey, BEHAVIOR_LIKE);
        if (reLike == null || reLike.equals("1")) {
            result.setIslike(false);
        } else {
            result.setIslike(true);
        }

        // 是否不喜欢
        String reUnLike = (String) cacheService.hGet(APARTICLE_CONTENT_UNLIKE + suffixKey, BEHAVIOR_UNLIKE);
        if (reUnLike == null || reUnLike.equals("1")) {
            result.setIsunlike(false);
        } else {
            result.setIsunlike(true);
        }

        // 是否收藏
        String reCollect = (String) cacheService.hGet(APARTICLE_CONTENT_COLLECT + suffixKey, BEHAVIOR_COLLECT);
        if (reCollect == null || reCollect.equals("1")) {
            result.setIscollection(false);
        } else {
            result.setIscollection(true);
        }

        // 是否关注
        String key = USER_FOLLOW_AUTHOR + ":" + user.getId();
        boolean isFollow = cacheService.sIsMember(key, dto.getAuthorId().toString());
        if (isFollow) {
            result.setIsfollow(true);
        } else {
            result.setIsfollow(false);
        }
        return ResponseResult.okResult(result);
    }

    /**
     * 加载首页
     * firstPage 是否是首页 false不是 true 是
     *
     * @param dto
     * @param type
     * @param firstPage
     */
    @Override
    public ResponseResult load2(ArticleHomeDto dto, Short type, boolean firstPage) {
        if (firstPage) {
            String stringJson = cacheService.get(HOT_ARTICLE_FIRST_PAGE + dto.getTag());
            if (StringUtils.isNotBlank(stringJson)) {
                List<HotArticleVo> hotArticleVoList = JSON.parseArray(stringJson, HotArticleVo.class);
                return ResponseResult.okResult(hotArticleVoList);
            }
        }
        return load(dto, type);
    }

    /**
     * 修改文章行为数据
     * type 1-likes  2-collection  3-comment  4-views
     */
    @Override
    public void updateArticleBehavior(ArticleUpdateDto dto) {
        String articleId = dto.getArticleId();
        int type = dto.getType();
        int num = dto.getNum();
        if (StringUtils.isBlank(articleId)) {
            return;
        }
        ApArticle article = getById(articleId);
        int likes = article.getLikes() == null ? 0 : article.getLikes();
        int collect = article.getCollection() == null ? 0 : article.getCollection();
        int comment = article.getComment() == null ? 0 : article.getComment();
        int views = article.getViews() == null ? 0 : article.getViews();
        switch (type) {
            case 1:
                likes += num;
                break;
            case 2:
                collect += num;
                break;
            case 3:
                comment += num;
            case 4:
                views += num;
        }
        article.setLikes(likes);
        article.setCollection(collect);
        article.setComment(comment);
        article.setViews(views);
        updateById(article);
    }

    /**
     * 更新文章行为数据
     *
     * @param mess
     */
    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        // 更新文章行为数据
        ApArticle apArticle = updateArticle(mess);
        // 计算分数
        Integer score = HotArticleServiceImpl.computeScore(apArticle);
        score = score * 3;

        // 更新当前频道的分值
        replaceDateToRedis(apArticle, score, HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId());

        // 更新推荐数据
        replaceDateToRedis(apArticle, score, HOT_ARTICLE_FIRST_PAGE + DEFAULT_TAG);
    }

    /**
     * 替换数据并且存入redis
     */
    private void replaceDateToRedis(ApArticle apArticle, Integer score, String s) {
        String articleListStr = cacheService.get(s);
        if (StringUtils.isNotBlank(articleListStr)) {
            List<HotArticleVo> hotArticleVoList = JSON.parseArray(articleListStr, HotArticleVo.class);
            boolean flag = true;

            // 如果缓存中存在，则更新分值
            for (HotArticleVo hotArticleVo : hotArticleVoList) {
                if (hotArticleVo.getId().equals(apArticle.getId())) {
                    hotArticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }

            // 如果不存在 比较分值
            if (flag) {
                if (hotArticleVoList.size() >= 30) {
                    hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore)
                            .thenComparing(HotArticleVo::getPublishTime).reversed()).collect(Collectors.toList());
                    HotArticleVo lastHot = hotArticleVoList.get(hotArticleVoList.size() - 1);
                    if (lastHot.getScore() < score) {
                        hotArticleVoList.remove(lastHot);
                        HotArticleVo hotArticleVo = new HotArticleVo();
                        BeanUtils.copyProperties(apArticle, hotArticleVo);
                        hotArticleVo.setScore(score);
                        hotArticleVoList.add(hotArticleVo);
                    }
                } else {
                    HotArticleVo hotArticleVo = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, hotArticleVo);
                    hotArticleVo.setScore(score);
                    hotArticleVoList.add(hotArticleVo);
                }
            }

            // 缓存到redis
            hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore)
                    .thenComparing(HotArticleVo::getPublishTime).reversed()).collect(Collectors.toList());
            cacheService.set(s, JSON.toJSONString(hotArticleVoList));
        }
    }

    /**
     * 更新文章行为数据
     * @param mess
     * @return
     */
    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        apArticle.setCollection(apArticle.getCollection() == null ? 0 : apArticle.getCollection() + mess.getCollect());
        apArticle.setComment(apArticle.getComment() == null ? 0 : apArticle.getComment() + mess.getComment());
        apArticle.setLikes(apArticle.getLikes() == null ? 0 : apArticle.getLikes() + mess.getLike());
        apArticle.setViews(apArticle.getViews() == null ? 0 : apArticle.getViews() + mess.getView());
        updateById(apArticle);
        return apArticle;
    }
}
