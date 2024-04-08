package com.heima.article.stream;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.article.mess.ArticleVisitStreamMess;
import com.heima.model.article.mess.UpdateArticleMess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class HotArticleStreamHandler {

    @Bean
    public KStream<String, String> kStream(StreamsBuilder streamsBuilder) {
        // 接受消息
        KStream<String, String> stream = streamsBuilder.stream(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC);
        // 聚合流式处理
        stream.map((key, value) -> {
                    UpdateArticleMess mess = JSON.parseObject(value, UpdateArticleMess.class);
                    // 重置消息的key value
                    return new KeyValue<>(mess.getArticleId().toString(), mess.getType() + ":" + mess.getAdd());
                })
                // 按照文章id进行聚合
                .groupBy((key, value) -> key)
                //时间窗口
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                // 自行聚合计算
                .aggregate(new Initializer<String>() {
                    /**
                     * 初始化变量
                     */
                    @Override
                    public String apply() {
                        return "COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0";
                    }
                }, new Aggregator<String, String, String>() {
                    /**
                     * 真正处理
                     */
                    @Override
                    public String apply(String key, String value, String aggValue) {
                        if (StringUtils.isBlank(value)) {
                            return aggValue;
                        }
                        String[] aggAry = aggValue.split(",");
                        int col = 0, com = 0, lik = 0, vie = 0;
                        for (String agg : aggAry) {
                            String[] split = agg.split(":");
                            switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])) {
                                case COLLECTION:
                                    col = Integer.parseInt(split[1]);
                                    break;
                                case COMMENT:
                                    com = Integer.parseInt(split[1]);
                                    break;
                                case LIKES:
                                    lik = Integer.parseInt(split[1]);
                                    break;
                                case VIEWS:
                                    vie = Integer.parseInt(split[1]);
                            }
                        }
                        /**
                         * 累加操作
                         */
                        String[] valAry = value.split(":");
                        switch (UpdateArticleMess.UpdateArticleType.valueOf(valAry[0])) {
                            case COLLECTION:
                                col += Integer.parseInt(valAry[1]);
                                break;
                            case COMMENT:
                                com += Integer.parseInt(valAry[1]);
                                break;
                            case LIKES:
                                lik += Integer.parseInt(valAry[1]);
                                break;
                            case VIEWS:
                                vie += Integer.parseInt(valAry[1]);
                        }
                        String formatStr = String.format("COLLECTION:%d,COMMENT:%d,LIKES:%d,VIEWS:%d", col, com, lik, vie);
                        log.info("文章的id:" + key);
                        log.info("文章的热度:" + formatStr);
                        return formatStr;
                    }
                }, Materialized.as("hot-article-stream-count-001"))
                .toStream()
                .map((key, value) -> {
                    return new KeyValue<>(key.key().toString(), formatObj(key.key().toString(), value));
                })
                .to(HotArticleConstants.HOT_ARTICLE_INCR_HANDLE_TOPIC);
        return stream;
    }

    /**
     * 格式化消息的value
     */
    private String formatObj(String articleId, String value) {
        ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
        mess.setArticleId(Long.valueOf(articleId));
        String[] valAry = value.split(",");
        for (String val : valAry) {
            String[] split = val.split(":");
            switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])) {
                case COLLECTION:
                    mess.setCollect(Integer.parseInt(split[1]));
                    break;
                case COMMENT:
                    mess.setComment(Integer.parseInt(split[1]));
                    break;
                case LIKES:
                    mess.setLike(Integer.parseInt(split[1]));
                    break;
                case VIEWS:
                    mess.setView(Integer.parseInt(split[1]));
            }
        }
        log.info("聚合消息处理之后的结果为:{}",JSON.toJSONString(mess));
        return JSON.toJSONString(mess);
    }
}
