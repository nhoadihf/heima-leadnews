package com.heima.apis.schedule.fallback;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.schedule.dtos.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IScheduleClientFallback implements IScheduleClient {
    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id
     */
    @Override
    public ResponseResult addTask(Task task) {
        log.error("远程调用超时---IScheduleClientFallback");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 取消任务
     *
     * @param taskId
     * @return
     */
    @Override
    public ResponseResult cancelTask(long taskId) {
        log.error("远程调用超时---IScheduleClientFallback");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 按照类型和优先级消费任务
     *
     * @param type
     * @param priority
     * @return
     */
    @Override
    public ResponseResult poll(int type, int priority) {
        log.error("远程调用超时---IScheduleClientFallback");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
