package com.heima.comment.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.user.IUserClient;
import com.heima.comment.pojos.ApComment;
import com.heima.comment.pojos.ApCommentLike;
import com.heima.comment.pojos.ApCommentRepay;
import com.heima.comment.pojos.vos.ApCommentRepayList;
import com.heima.comment.service.ApCommentReplyService;
import com.heima.model.comment.dtos.CommentRepayDto;
import com.heima.model.comment.dtos.CommentRepayLikeDto;
import com.heima.model.comment.dtos.CommentRepaySaveDto;
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
public class ApCommentReplyServiceImpl implements ApCommentReplyService {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private IUserClient userClient;

    /**
     * 加载评论回复
     */
    @Override
    public ResponseResult loadList(CommentRepayDto dto) {
        if (StringUtils.isBlank(dto.getCommentId())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.setSize(50);

        ApUser apUser = AppThreadLocalUtil.getWmUser();
        if (apUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        Query query = Query.query(Criteria.where("commentId").is(dto.getCommentId())
                .and("createdTime").lt(dto.getMinDate()));
        query.limit(dto.getSize());
        List<ApCommentRepayList> repayLists = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "createdTime")), ApCommentRepayList.class);
        if (!repayLists.isEmpty()) {
            for (ApCommentRepayList repayList : repayLists) {
                // 查询是否点赞
                Query queryLike = Query.query(Criteria.where("authorId").is(apUser.getId())
                        .and("commentId").is(repayList.getId()));
                ApCommentLike like = mongoTemplate.findOne(queryLike, ApCommentLike.class);
                if (like != null) {
                    repayList.setOperation(0);
                }
                ResponseResult responseResult = userClient.getUser(repayList.getAuthorId());
                if (responseResult.getCode().equals(200)) {
                    ApUser user = JSON.parseObject(JSON.toJSONString(responseResult.getData()), ApUser.class);
                    repayList.setAuthorImage(user.getImage());
                }
            }
        }
        return ResponseResult.okResult(repayLists);
    }

    /**
     * 回复评论
     */
    @Override
    public ResponseResult saveComm(CommentRepaySaveDto dto) {
        if (StringUtils.isBlank(dto.getCommentId()) || StringUtils.isBlank(dto.getContent())
                || dto.getContent().length() > 140) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser apUser = AppThreadLocalUtil.getWmUser();
        if (apUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        ResponseResult responseResult = userClient.getUser(apUser.getId());
        if (!responseResult.getCode().equals(200)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN, "用户不存在");
        }
        apUser = JSON.parseObject(JSON.toJSONString(responseResult.getData()), ApUser.class);
        // 更新回复数
        Query comQuery = Query.query(Criteria.where("id").is(dto.getCommentId()));
        ApComment apComment = mongoTemplate.findOne(comQuery, ApComment.class);
        if (apComment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "当前评论不存在");
        }
        apComment.setReply((apComment.getReply() == null ? 0 : apComment.getReply()) + 1);
        mongoTemplate.save(apComment);

        // 插入回复表
        ApCommentRepay repay = new ApCommentRepay();
        repay.setLikes(0);
        repay.setAuthorName(apUser.getName());
        repay.setAuthorId(apUser.getId());
        repay.setContent(dto.getContent());
        repay.setCreatedTime(new Date());
        repay.setUpdatedTime(new Date());
        repay.setCommentId(dto.getCommentId());
        mongoTemplate.save(repay);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 评论回复点赞
     */
    @Override
    public ResponseResult likeRepay(CommentRepayLikeDto dto) {
        if (StringUtils.isBlank(dto.getCommentRepayId()) ||
                dto.getOperation() < 0 || dto.getOperation() > 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser apUser = AppThreadLocalUtil.getWmUser();
        if (apUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        Query queryRepay = Query.query(Criteria.where("id").is(dto.getCommentRepayId()));
        ApCommentRepay repay = mongoTemplate.findOne(queryRepay, ApCommentRepay.class);
        if (repay == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 查询是否点赞
        Query queryLike = Query.query(Criteria.where("commentId").is(dto.getCommentRepayId())
                .and("authorId").is(apUser.getId()));
        ApCommentLike commentLike = mongoTemplate.findOne(queryLike, ApCommentLike.class);

        if (dto.getOperation() == 0) {
            // 点赞
            if (commentLike != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "已经点赞");
            }
            ApCommentLike like = new ApCommentLike();
            like.setCommentId(dto.getCommentRepayId());
            like.setAuthorId(apUser.getId());
            mongoTemplate.save(like);

            // 更新点赞数
            repay.setLikes((repay.getLikes() == null ? 0 : repay.getLikes()) + 1);
        } else {
            // 取消点赞
            if (commentLike == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "没有点赞");
            }
            // 删除点赞记录
            mongoTemplate.remove(commentLike);

            // 更新点赞数
            repay.setLikes((repay.getLikes() == null ? 1 : repay.getLikes()) - 1);
        }

        mongoTemplate.save(repay);

        Map<String, Object> map = new HashMap<>();
        map.put("likes", repay.getLikes());
        return ResponseResult.okResult(map);
    }
}
