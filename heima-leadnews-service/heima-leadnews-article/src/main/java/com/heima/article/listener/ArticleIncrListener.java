package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.article.mess.ArticleVisitStreamMess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ArticleIncrListener {
    @Resource
    private ApArticleService apArticleService;

    @KafkaListener(topics = HotArticleConstants.HOT_ARTICLE_INCR_HANDLE_TOPIC)
    public void onMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            ArticleVisitStreamMess mess = JSON.parseObject(message, ArticleVisitStreamMess.class);
            log.info("开始更新热文章行为数据-ArticleIncrListener");
            apArticleService.updateScore(mess);
        }
    }
}
