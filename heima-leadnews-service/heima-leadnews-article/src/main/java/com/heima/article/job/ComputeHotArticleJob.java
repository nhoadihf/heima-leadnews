package com.heima.article.job;

import com.heima.article.service.HotArticleService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ComputeHotArticleJob {
    @Resource
    private HotArticleService hotArticleService;

    @XxlJob("computeHotArticleJob")
    public void handle() {
        log.info("热文章分值计算开始...");
        hotArticleService.computeHotArticle();
    }
}
