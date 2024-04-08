package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleService apArticleService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 上传文章静态文件到minio
     *
     * @param apArticle
     * @param content
     */
    @Override
    @Async
    public void buildArticleToMinIO(ApArticle apArticle, String content) {
        StringWriter out = new StringWriter();
        try {
            if (StringUtils.isNotBlank(content)) {
                Template template = configuration.getTemplate("article.ftl");

                Map<String, Object> map = new HashMap<>();
                map.put("content", JSONArray.parseArray(content));
                map.put("authorImage", apArticle.getAuthorImage() == null ? "" : apArticle.getAuthorImage());

                template.process(map, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        InputStream in = new ByteArrayInputStream(out.toString().getBytes());
        String path = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", in);

        apArticleService.update(Wrappers.<ApArticle>lambdaUpdate().eq(ApArticle::getId, apArticle.getId()).set(ApArticle::getStaticUrl, path));

        // 发送消息创建索引
        createArticleEsIndex(apArticle, content, path);
    }

    /**
     * 发送消息，创建索引
     *
     * @param apArticle
     * @param content
     * @param path
     */
    private void createArticleEsIndex(ApArticle apArticle, String content, String path) {
        SearchArticleVo searchArticleVo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, searchArticleVo);
        searchArticleVo.setContent(content);
        searchArticleVo.setStaticUrl(path);

        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, JSON.toJSONString(searchArticleVo));
    }
}
