package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    private MediaFileService mediaFileService;


    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getpreivewVideo(@PathVariable("mediaId") String mediaId) {
        MediaFiles file = mediaFileService.getFileById(mediaId);
        if (file == null) {
            return RestResponse.validfail("视频找不到");
        }

        String url = file.getUrl();
        if (StringUtils.isEmpty(url)) {
            return RestResponse.validfail("视频正在处理中");
        }
        return RestResponse.success(url);
    }


}
