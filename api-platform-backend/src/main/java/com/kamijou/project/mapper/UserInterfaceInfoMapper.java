package com.kamijou.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kamijou.apicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author kamijou
* @description 针对表【user_interface_info(接口信息)】的数据库操作Mapper
* @createDate 2024-04-06 12:10:18
* @Entity com.kamijou.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




