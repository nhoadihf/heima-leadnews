package com.heima.comment.pojos;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document("ap_comment_repay")
public class ApCommentRepay implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private Integer authorId;

    private String authorName;

    private String commentId;

    private String content;

    private Integer likes;

    private Date createdTime;

    private Date updatedTime;
}
