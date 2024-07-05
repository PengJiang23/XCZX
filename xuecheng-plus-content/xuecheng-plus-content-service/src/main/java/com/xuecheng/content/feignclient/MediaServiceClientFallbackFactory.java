package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import groovy.util.Factory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


/**
 * 使用fallback factory 实现降级
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String objectName) {
               log.debug("远程调用媒资服务上传静态资源，失败", throwable.toString(),throwable);
                return "";
            }
        };
    }
}
