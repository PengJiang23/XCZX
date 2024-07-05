package com.xuecheng.content;


import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

@SpringBootTest
public class FreeMarkerTest {


    @Autowired
    CoursePublishService coursePublishService;

    @Test
    public void generateHtml() throws Exception{


        Configuration configuration = new Configuration(Configuration.getVersion());

        //拿到classpath路径
        String classpath = this.getClass().getResource("/").getPath();
        //指定模板的目录
        configuration.setDirectoryForTemplateLoading(new File(classpath+"/templates/"));
        //指定编码
        configuration.setDefaultEncoding("utf-8");

        //得到模板
        Template template = configuration.getTemplate("course_template.ftl");
        //准备数据
        CoursePreviewDTO coursePreviewInfo = coursePublishService.GetCourseInfo(120L);
        HashMap<String, Object> map = new HashMap<>();
        map.put("model",coursePreviewInfo);



        //Template template 模板, Object model 数据
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //输入流
        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
        //输出文件
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\develop\\120.html"));
        //使用流将html写入文件
        IOUtils.copy(inputStream,outputStream);


    }

}
