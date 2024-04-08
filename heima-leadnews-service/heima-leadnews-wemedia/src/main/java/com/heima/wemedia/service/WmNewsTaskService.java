package com.heima.wemedia.service;

import java.util.Date;

public interface WmNewsTaskService {

    /**
     * 发布任务到延迟队列中
     * @param id 文章id
     * @param publishTime 发布时间 当 任务的执行时间
     */
    void addNewsToTask(Integer id, Date publishTime);

    /**
     * 消费任务
     */
    void scanNewsByTask();
}
