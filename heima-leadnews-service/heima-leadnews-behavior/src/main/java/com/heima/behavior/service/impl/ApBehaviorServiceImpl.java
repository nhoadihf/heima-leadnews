package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.behavior.service.ApBehaviorService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleUpdateDto;
import com.heima.model.article.mess.UpdateArticleMess;
import com.heima.model.behavior.dtos.BeLikesDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.heima.common.constants.BehaviorConstants.*;

@Service
@Slf4j
public class ApBehaviorServiceImpl implements ApBehaviorService {
    @Resource
    private CacheService cacheService;
    @Resource
    private IArticleClient articleClient;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 点赞
     *
     * @param dto
     */
    @Override
    public ResponseResult likesBehavior(BeLikesDto dto) {
        // 获取当前登录用户
        ApUser user = AppThreadLocalUtil.getWmUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        if (StringUtils.isBlank(dto.getArticleId()) || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        String key = APARTICLE_CONTENT_LIKE + ":" + dto.getArticleId() + ":" + user.getId();

        // 修改状态
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(Long.valueOf(dto.getArticleId()));
        mess.setType(UpdateArticleMess.UpdateArticleType.LIKES);

        if (dto.getOperation() == 0) {
            Object operation = cacheService.hGet(key, BEHAVIOR_LIKE);
            if (operation != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已经点赞");
            }
            mess.setAdd(1);
            cacheService.hPut(key, BEHAVIOR_LIKE, String.valueOf(dto.getOperation()));
        } else {
            mess.setAdd(-1);
            cacheService.hDelete(key, BEHAVIOR_LIKE);
        }

        // 发送消息数据聚合
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 文章不喜欢
     *
     * @param dto
     */
    @Override
    public ResponseResult unLikesBehavior(UnLikesBehaviorDto dto) {
        // 获取当前登录用户
        ApUser user = AppThreadLocalUtil.getWmUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        if (StringUtils.isBlank(dto.getArticleId()) || dto.getType() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        String key = APARTICLE_CONTENT_UNLIKE + ":" + dto.getArticleId() + ":" + user.getId();

        // 修改状态
        cacheService.hPut(key, BEHAVIOR_UNLIKE, String.valueOf(dto.getType()));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 修改阅读次数
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult readBehavior(ReadBehaviorDto dto) {
        if (StringUtils.isBlank(dto.getArticleId()) || dto.getCount() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = AppThreadLocalUtil.getWmUser();
        Integer count = 0;
        if (user.getId() != null) {
            String key = USER_READ_ARTICLE_COUNT + ":" + dto.getArticleId() + ":" + user.getId();
            String reCount = cacheService.get(key);
            if (reCount != null) {
                count = Integer.parseInt(reCount) + dto.getCount();
            } else {
                count = 1;
                ArticleUpdateDto updateDto = new ArticleUpdateDto(dto.getArticleId(), 4, 1);
                articleClient.updateArticleBehavior(updateDto);
            }
            cacheService.set(key, String.valueOf(count));

            UpdateArticleMess mess = new UpdateArticleMess();
            mess.setArticleId(Long.valueOf(dto.getArticleId()));
            mess.setType(UpdateArticleMess.UpdateArticleType.VIEWS);
            mess.setAdd(1);

            kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));
        }
        return ResponseResult.okResult(count);
    }
}
