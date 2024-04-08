package com.heima.comment.pojos;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document("ap_comment")
public class ApComment implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private String id;

    /**
     * 评论人id
     */
    private Integer authorId;

    /**
     * 评论人姓名
     */
    private String authorName;

    /**
     * 文章id
     */
    private Long entryId;

    /**
     * 0-文章评论
     */
    private Short type;

    private String content;

    private Integer likes;

    private Integer reply;

    private Short flag;

    private Date createdTime;
}
