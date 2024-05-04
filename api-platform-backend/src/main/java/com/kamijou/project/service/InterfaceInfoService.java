package com.kamijou.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kamijou.apicommon.model.entity.InterfaceInfo;

/**
* @author kamijou
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-03-24 09:58:40
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
