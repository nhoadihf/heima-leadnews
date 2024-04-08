package com.heima.wemedia;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliYunTest {
    @Resource
    private GreenTextScan greenTextScan;

    @Resource
    private GreenImageScan greenImageScan;

    @Resource
    private FileStorageService fileStorageService;

    @Test
    public void testScanText() throws Exception {
        Map map = greenTextScan.greeTextScan("我是一个俗人");
        System.out.println(map);
    }

    @Test
    public void testScanImgae() throws Exception {
        byte[] bytes = fileStorageService.downLoadFile("http://192.168.200.130:9000/leadnews/2024/01/24/89232561f2e3415db823ea353b6353d7.jpg");

        List<byte[]> list = new ArrayList<>();
        list.add(bytes);
        Map map = greenImageScan.imageScan(list);
        System.out.println(map);
    }
}
