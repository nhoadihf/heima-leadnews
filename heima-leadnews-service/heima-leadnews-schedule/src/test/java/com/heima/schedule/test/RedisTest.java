package com.heima.schedule.test;

import com.heima.common.redis.CacheService;
import com.heima.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Resource
    private CacheService cacheService;

    @Test
    public void testList() {
        //cacheService.lLeftPush("list_key_001", "hello_list");
        String value = cacheService.lRightPop("list_key_001");
        System.out.println(value);
    }

    @Test
    public void TestZset() {

    }
}
