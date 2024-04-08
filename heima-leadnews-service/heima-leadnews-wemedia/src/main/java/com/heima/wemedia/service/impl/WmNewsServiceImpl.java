package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.article.IArticleClient;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.*;
import com.heima.model.wemedia.pojos.*;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.*;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Resource
    private WmUserMapper wmUserMapper;
    @Resource
    private IArticleClient articleClient;
    @Resource
    private WmChannelMapper wmChannelMapper;

    /**
     * 查询文章列表
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDto dto) {
        // 效验参数
        dto.checkParam();
        // 分页查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 查询状态
        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }
        // 查询频道列表
        if (dto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }
        // 查询时间
        if (dto.getBeginPubDate() != null && dto.getEndPubDate() != null) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }
        // 模糊查询标题
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyword());
        }
        // 查询当前登录人
        lambdaQueryWrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getWmUser().getId());

        // 倒叙排序
        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);
        page = page(page, lambdaQueryWrapper);
        // 返回参数
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    @Resource
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Resource
    private WmNewsTaskService wmNewsTaskService;

    /**
     * 发布文章修改与保存草稿
     * param dto
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        // 条件判断
        if (dto == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 保存或修改草稿
        WmNews wmNews = new WmNews();
        // 属性拷贝
        BeanUtils.copyProperties(dto, wmNews);
        // 设置封面图片
        if (dto.getImages() != null && dto.getImages().size() != 0) {
            String imageStr = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(imageStr);
        }
        // 如果封面类型为-1
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);
        // 判断是否为草稿，是则退出
        if (dto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        // 不是，保存文章内容图片与素材的关系
        List<String> materials = extractUrlInfo(dto.getContent());
        saveRelativeInfoForContent(materials, wmNews.getId());

        // 不是草稿，保存文章封面图片与素材的关系
        saveRelativeInfoForCover(dto, wmNews, materials);

        // 异步审核
        //wmNewsAutoScanService.autoScan(wmNews.getId());
        wmNewsTaskService.addNewsToTask(wmNews.getId(), wmNews.getPublishTime());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 第一个功能：如果当前封面类型为自动，则设置封面类型的数据
     * 匹配规则：
     * 1，如果内容图片大于等于1，小于3  单图  type 1
     * 2，如果内容图片大于等于3  多图  type 3
     * 3，如果内容没有图片，无图  type 0
     * <p>
     * 第二个功能：保存封面图片与素材的关系
     *
     * @param dto
     * @param wmNews
     * @param materials
     */
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {
        List<String> images = dto.getImages();
        // 如果当前封面类型为自动，则设置封面类型的数据
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            if (materials.size() >= 3) {
                // 多图
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else if (materials.size() >= 1 && materials.size() < 3) {
                // 单图
                wmNews.setType(WemediaConstants.WM_COVER_REFERENCE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            } else {
                // 无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
            // 修改文章
            if (images != null && images.size() > 0) {
                wmNews.setImages(StringUtils.join(images, ","));
            }
            updateById(wmNews);
        }

        // 保存封面图片与素材的关系
        if (images != null && images.size() > 0) {
            saveRelativeInfo(images, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    /**
     * 保存文章内容图片与素材的关系
     *
     * @param materials
     * @param newsId
     */
    private void saveRelativeInfoForContent(List<String> materials, Integer newsId) {
        saveRelativeInfo(materials, newsId, WemediaConstants.WM_CONTENT_REFERENCE);
    }

    @Resource
    private WmMaterialMapper wmMaterialMapper;

    /**
     * 批量素材保存关系
     *
     * @param materials
     * @param newsId
     * @param type
     */
    private void saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        if (materials != null && !materials.isEmpty()) {
            // 根据url查询id
            List<WmMaterial> dbMaterial = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, materials));

            List<Integer> idList = dbMaterial.stream().map(WmMaterial::getId).collect(Collectors.toList());

            if (dbMaterial == null || dbMaterial.size() == 0) {
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }

            if (idList == null || idList.size() != dbMaterial.size()) {
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }

            wmNewsMaterialMapper.saveRelations(idList, newsId, type);
        }
    }

    /**
     * 提取文章中的图片
     */
    private List<String> extractUrlInfo(String content) {
        List<Map> maps = JSON.parseArray(content, Map.class);
        List<String> materials = new ArrayList<>();
        for (Map map : maps) {
            if (map.get("type").equals("image")) {
                String imageUrl = (String) map.get("value");
                materials.add(imageUrl);
            }
        }
        return materials;
    }

    @Resource
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 保存或修改文章
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        // 设置属性
        wmNews.setUserId(WmThreadLocalUtil.getWmUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1); // 默认上架

        if (wmNews.getId() == null) {
            // 保存
            save(wmNews);
        } else {
            // 修改
            // 删除关联关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }
    }

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        // 效验参数
        if (dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }

        // 判断文章是否发布
        if (!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章没有发布，不能上下架");
        }

        // 修改文章enable
        if (dto.getEnable() != null && dto.getEnable() > -1 && dto.getEnable() < 2) {
            update(Wrappers.<WmNews>lambdaUpdate().set(WmNews::getEnable, dto.getEnable())
                    .eq(WmNews::getId, dto.getId()));

            // 发送消息，通知article端修改文章配置
            if (wmNews.getArticleId() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("articleId", wmNews.getArticleId());
                map.put("enable", dto.getEnable());
                kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC, JSON.toJSONString(map));
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 平台后台分页查询文章
     *
     * @param dto
     */
    @Override
    public ResponseResult listByDto(AdWmNewsSearchDto dto) {
        // 效验参数
        dto.checkParam();
        // 分页查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(dto.getTitle())) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getTitle());
        }

        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }

        // 时间排序
        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);

        page = page(page, lambdaQueryWrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        List<WmNews> records = page.getRecords();
        List<AdWmNewsDto> result = new ArrayList<>();
        for (WmNews record : records) {
            WmUser user = wmUserMapper.selectById(record.getUserId());
            AdWmNewsDto adWmNewsDto = new AdWmNewsDto();
            BeanUtils.copyProperties(record, adWmNewsDto);
            if (user != null) {
                adWmNewsDto.setAuthorName(user.getName());
            }
            result.add(adWmNewsDto);
        }
        responseResult.setData(result);
        return responseResult;
    }

    /**
     * 平台后台查看文章详情
     *
     * @param id
     */
    @Override
    public ResponseResult showDetail(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章不存在");
        }

        WmUser user = wmUserMapper.selectById(wmNews.getUserId());
        AdWmNewsDto adWmNewsDto = new AdWmNewsDto();
        BeanUtils.copyProperties(wmNews, adWmNewsDto);
        if (user != null) {
            adWmNewsDto.setAuthorName(user.getName());
        }
        return ResponseResult.okResult(adWmNewsDto);
    }

    /**
     * 审核成功
     *
     * @param dto
     */
    @Override
    public ResponseResult authPass(AdWmNewsAuthDto dto) {
        if (dto.getId() == null || dto.getStatus() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章不存在");
        }
        wmNews.setStatus(dto.getStatus());

        // 保存app端文章
        ArticleDto apArticle = new ArticleDto();
        BeanUtils.copyProperties(wmNews, apArticle);
        // 文章的布局
        apArticle.setLayout(wmNews.getType());
        // 频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            apArticle.setChannelName(wmChannel.getName());
        }
        // 作者
        apArticle.setAuthorId(wmNews.getUserId().longValue());
        WmUser user = wmUserMapper.selectById(wmNews.getUserId());
        if (user != null) {
            apArticle.setAuthorName(user.getName());
            apArticle.setAuthorImage(user.getImage());
        }
        // 设置文章id
        if (wmNews.getArticleId() != null) {
            apArticle.setId(wmNews.getArticleId());
        }
        apArticle.setCreatedTime(new Date());
        ResponseResult result = articleClient.saveArticle(apArticle);
        if (result.getCode().equals(200)) {
            wmNews.setArticleId((Long) result.getData());
            updateById(wmNews);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 审核失败
     *
     * @param dto
     */
    @Override
    public ResponseResult authFail(AdWmNewsAuthDto dto) {
        if (dto.getId() == null || dto.getStatus() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章不存在");
        }
        wmNews.setStatus(dto.getStatus());
        wmNews.setReason(dto.getMsg());
        updateById(wmNews);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
