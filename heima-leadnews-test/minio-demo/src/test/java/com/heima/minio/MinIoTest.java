package com.heima.minio;

import com.heima.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;

/**
 * @author moningxi
 * @date 2024/1/4
 */
@SpringBootTest(classes = MinIoApplication.class)
@RunWith(SpringRunner.class)
public class MinIoTest {

    @Autowired
    private FileStorageService fileStorageService;

/*    @Test
    public void test() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("D:\\list.html");
        String path = fileStorageService.uploadHtmlFile("", "list.html", fileInputStream);
        System.out.println(path);
    }*/

    @Test
    public void test() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream("D:\\temp\\index.css");
            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123").endpoint("http://192.168.200.130:9000").build();
            // 上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("plugins/css/index.css") // 文件名
                    .contentType("text/css") // 文件类型
                    .bucket("leadnews") // 桶名称
                    .stream(fileInputStream, fileInputStream.available(), -1) // 文件流
                    .build();
            minioClient.putObject(putObjectArgs);

            System.out.println("http://192.168.200.130:9000/leadnews/list.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
