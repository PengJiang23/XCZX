package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaProcessService {

    public List<MediaProcess> getByMediaProcess(int shardTotal, int shardIndex, int count);

    public boolean startTask(Long id);

    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

}
