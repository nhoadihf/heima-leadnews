package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.pojos.ApAssociateWords;
import com.heima.search.service.ApAssociateWordsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class ApAssociateWordsServiceImpl implements ApAssociateWordsService {
    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 搜索联想词
     * @param dto
     * @return
     */
    @Override
    public ResponseResult search(UserSearchDto dto) {
        // 校验参数
        if (dto == null || StringUtils.isBlank(dto.getSearchWords())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 分页检查
        if (dto.getPageSize() > 20) {
            dto.setPageSize(20);
        }

        // 设置查询条件 模糊查询
        Query query = Query.query(Criteria.where("associateWords").regex(".*\\" + dto.getSearchWords() + ".*"));
        query.limit(dto.getPageSize());

        List<ApAssociateWords> apAssociateWords = mongoTemplate.find(query, ApAssociateWords.class);
        return ResponseResult.okResult(apAssociateWords);
    }
}
