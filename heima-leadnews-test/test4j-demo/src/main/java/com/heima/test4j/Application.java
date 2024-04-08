package com.heima.test4j;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class Application {

    /**
     * 图片识别
     * @param args
     */
    public static void main(String[] args) throws TesseractException {
        //创建Tesseract对象
        ITesseract tesseract = new Tesseract();
        // 设置字体库路径
        tesseract.setDatapath("D:\\heima\\heima_headlines\\test4j");
        // 设置语言
        tesseract.setLanguage("chi_sim");
        // 获取图片
        String result = tesseract.doOCR(new File("C:\\Users\\AYXY\\Pictures\\Camera Roll\\QQ截图20240126085338.png"));
        // 替换回车换行
        result = result.replaceAll("\\r|\\n", "-").replaceAll(" ","");
        System.out.println("识别的结果：" + result);
    }
}
