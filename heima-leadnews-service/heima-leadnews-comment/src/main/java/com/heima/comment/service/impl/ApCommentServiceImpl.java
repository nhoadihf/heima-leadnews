package com.heima.comment.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.user.IUserClient;
import com.heima.comment.pojos.ApComment;
import com.heima.comment.pojos.ApCommentLike;
import com.heima.comment.pojos.vos.ApCommentList;
import com.heima.comment.service.ApCommentService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.article.mess.UpdateArticleMess;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ApCommentServiceImpl implements ApCommentService {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private IUserClient userClient;
    @Resource
    private KafkaTemplate kafkaTemplate;

    /**
     * 保存用户评论
     */
    @Override
    public ResponseResult saveComment(CommentSaveDto dto) {
        ApComment apComment = new ApComment();
        if (dto.getArticleId() == null || dto.getContent().length() > 140 || StringUtils.isBlank(dto.getContent())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser apUser = AppThreadLocalUtil.getWmUser();
        if (apUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        ResponseResult responseResult = userClient.getUser(apUser.getId());
        if (!responseResult.getCode().equals(200)) {
            return responseResult;
        }
        ApUser user = JSON.parseObject(JSON.toJSONString(responseResult.getData()), ApUser.class);

        apComment.setLikes(0);
        apComment.setFlag((short) 0);
        apComment.setCreatedTime(new Date());
        apComment.setEntryId(dto.getArticleId());
        apComment.setContent(dto.getContent());
        apComment.setAuthorId(user.getId());
        apComment.setReply(0);
        apComment.setAuthorName(user.getName());
        apComment.setType((short) 0);
        mongoTemplate.save(apComment);

        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setAdd(1);
        mess.setArticleId(dto.getArticleId());
        mess.setType(UpdateArticleMess.UpdateArticleType.COMMENT);

        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 加载文章评论
     */
    @Override
    public ResponseResult loadComm(CommentDto dto) {
        if (dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser apUser = AppThreadLocalUtil.getWmUser();
        Query query = Query.query(Criteria.where("entryId").is(dto.getArticleId())
                .and("createdTime").lt(dto.getMinDate()));
        query.limit(10);
        List<ApCommentList> apCommentLists = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "createdTime", "likes")), ApCommentList.class);
        if (apUser != null && apCommentLists != null && apCommentLists.size() > 0) {
            for (ApCommentList apCommentList : apCommentLists) {
                Query queryLike = Query.query(Criteria.where("authorId").is(apUser.getId())
                        .and("commentId").is(apCommentList.getId()));
                // 查询是否点赞
                ApCommentLike like = mongoTemplate.findOne(queryLike, ApCommentLike.class);
                if (like != null) {
                    apCommentList.setOperation(0);
                }
                // 设置头像
                ResponseResult responseResult = userClient.getUser(apCommentList.getAuthorId());
                if (responseResult.getCode().equals(200)) {
                    ApUser user = JSON.parseObject(JSON.toJSONString(responseResult.getData()), ApUser.class);
                    apCommentList.setAuthorImage(user.getImage());
                }
            }
        }
        return ResponseResult.okResult(apCommentLists);
    }

    /**
     * 评论点赞
     *
     * @param dto
     */
    @Override
    public ResponseResult liekComm(CommentLikeDto dto) {
        if (StringUtils.isBlank(dto.getCommentId()) || dto.getOperation() < 0 || dto.getOperation() > 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser apUser = AppThreadLocalUtil.getWmUser();
        if (apUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Query queryComm = Query.query(Criteria.where("id").is(dto.getCommentId()));
        ApComment apComment = mongoTemplate.findOne(queryComm, ApComment.class);
        if (apComment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论不存在");
        }

        Query queryLike = Query.query(Criteria.where("authorId").is(apUser.getId())
                .and("commentId").is(dto.getCommentId()));
        ApCommentLike like = mongoTemplate.findOne(queryLike, ApCommentLike.class);

        if (dto.getOperation() == 0) {
            // 点赞
            if (like != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已经点赞");
            }
            like = new ApCommentLike();
            like.setAuthorId(apUser.getId());
            like.setCommentId(dto.getCommentId());
            mongoTemplate.save(like);
            apComment.setLikes((apComment.getLikes() == null ? 0 : apComment.getLikes()) + 1);
        } else {
            // 取消点赞
            if (like == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "没有点赞");
            }
            mongoTemplate.remove(like);

            apComment.setLikes((apComment.getLikes() > 0 ? apComment.getLikes() : 1) - 1);
        }
        mongoTemplate.save(apComment);
        Map<String, Object> map = new HashMap<>();
        map.put("likes", apComment.getLikes());
        return ResponseResult.okResult(map);
    }
}
