package com.heima.apis.schedule;

import com.heima.apis.schedule.fallback.IScheduleClientFallback;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "leadnews-schedule", fallback = IScheduleClientFallback.class)
public interface IScheduleClient {
    /**
     * 添加任务
     * @param task   任务对象
     * @return 任务id
     */
    @PostMapping("/api/v1/task/add")
    ResponseResult addTask(@RequestBody Task task);

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @GetMapping("/api/v1/task/{taskId}")
    ResponseResult cancelTask(@PathVariable("taskId") long taskId);

    /**
     * 按照类型和优先级消费任务
     * @param type
     * @param priority
     * @return
     */
    @GetMapping("/api/v1/task/{type}/{priority}")
    ResponseResult poll(@PathVariable("type") int type, @PathVariable("priority") int priority);
}
