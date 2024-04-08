package com.heima.comment.pojos;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document("ap_comment_like")
public class ApCommentLike implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer authorId;
    private String commentId;
}
