package com.kamijou.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kamijou.apicommon.model.entity.UserInterfaceInfo;

public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 调用接口统计
     *
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
