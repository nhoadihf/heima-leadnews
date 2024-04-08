package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.test4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Resource
    private WmNewsMapper wmNewsMapper;

    /**
     * 自媒体文章审核
     *
     * @param id 自媒体文章id
     */
    @Override
    @Async // 开启异步调用
    public void autoScan(Integer id) {
        // 效验参数
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }

        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            // 从内容中提取内容和图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);
            // 审核内容
            boolean isTextScan = handleScanText((String) textAndImages.get("content"), wmNews);
            if (!isTextScan) return;

            // 自定义敏感词过滤
            boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
            if (!isSensitive) return;

            // 审核图片
            boolean isImageScan = handleScanImages((List<String>) textAndImages.get("images"), wmNews);
            if (!isImageScan) return;
            // 审核成功保存app端的文章
            ResponseResult responseResult = saveApArticle(wmNews);
            if (!responseResult.getCode().equals(200)) {
                throw new RuntimeException("WmNewsAutoScanServiceImpl-保存APP端文章失败");
            }

            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews, (short) 9, "审核成功");
        }


    }

    @Resource
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 敏感词过滤
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag = true;
        // 获取敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        // 初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        Map<String, Integer> maps = SensitiveWordUtil.matchWords(content);
        if (maps.size() > 0) {
            updateWmNews(wmNews, (short) 2, "当前内容存在违规内容" + maps);
            flag = false;
        }

        return flag;
    }

    @Resource
    private IArticleClient iArticleClient;
    @Resource
    private WmChannelMapper wmChannelMapper;
    @Resource
    private WmUserMapper wmUserMapper;

    /**
     * 保存app端的文章
     * @param wmNews
     * @return
     */
    private ResponseResult saveApArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews, dto);
        // 文章的布局
        dto.setLayout(wmNews.getType());
        // 频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            dto.setChannelName(wmChannel.getName());
        }
        // 作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser user = wmUserMapper.selectById(wmNews.getUserId());
        if (user != null) {
            dto.setAuthorName(user.getName());
            dto.setAuthorImage(user.getImage());
        }
        // 设置文章id
        if (wmNews.getArticleId() != null) {
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());
        ResponseResult responseResult = iArticleClient.saveArticle(dto);
        return responseResult;
    }

    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private GreenImageScan greenImageScan;
    @Resource
    private Tess4jClient tess4jClient;

    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleScanImages(List<String> images, WmNews wmNews) {

        boolean flag = true;
        // 图片去重
        images = images.stream().distinct().collect(Collectors.toList());
        if (images == null || images.size() == 0) {
            return flag;
        }

        List<byte[]> imageList = new ArrayList<>();
        try {
            // 下载图片
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);
                // 将byte转成BufferedImage
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(in);
                // 审核图片文字
                String result = tess4jClient.doOCR(bufferedImage);
                boolean isSensitive = handleSensitiveScan(result, wmNews);
                if(!isSensitive) return isSensitive;

                imageList.add(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 审核
/*        try {
            Map map = greenImageScan.imageScan(imageList);
            if (map != null) {
                if (map.get("suggestion").equals("block")) {
                    // 审核失败
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                }

                if (map.get("suggestion").equals("review")) {
                    // 人工审核
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }*/
        return flag;
    }

    @Resource
    private GreenTextScan greenTextScan;

    /**
     * 审核内容
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleScanText(String content, WmNews wmNews) {
        boolean flag = true;

        if ((wmNews.getTitle() + content).length() == 0) {
            return true;
        }

        try {
            Map map = greenTextScan.greeTextScan(wmNews.getTitle() + content);
            if (map != null) {
                if (map.get("suggestion").equals("block")) {
                    // 审核失败
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                }

                if (map.get("suggestion").equals("review")) {
                    // 人工审核
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 更新文章状态
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, Short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 从内容中提取文本和图片
     * 从封面提取图片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        // 存储纯文本内容
        StringBuilder stringBuilder = new StringBuilder();

        List<String> images = new ArrayList<>();

        // 从内容中提取文本和图片
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    stringBuilder.append(map.get("value"));
                }

                if (map.get("type").equals("image")) {
                    images.add((String) map.get("value"));
                }
            }
        }

        // 从封面提取图片
        String[] split = wmNews.getImages().split(",");
        images.addAll(Arrays.asList(split));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder.toString());
        resultMap.put("images", images);
        return resultMap;
    }
}
