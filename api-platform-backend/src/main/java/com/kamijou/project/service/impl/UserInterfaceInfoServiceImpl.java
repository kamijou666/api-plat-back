package com.kamijou.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kamijou.apicommon.model.entity.UserInterfaceInfo;
import com.kamijou.project.common.ErrorCode;
import com.kamijou.project.exception.BusinessException;
import com.kamijou.project.mapper.UserInterfaceInfoMapper;
import com.kamijou.project.service.UserInterfaceInfoService;
import org.springframework.stereotype.Service;

/**
 * @author kamijou
 * @description 针对表【user_interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2024-04-06 12:10:18
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");

            }
        }
        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足");
        }


    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UpdateWrapper<UserInterfaceInfo> queryWrapper = new UpdateWrapper<>();


        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        queryWrapper.eq("userId", userId);
        queryWrapper.setSql("leftNum=leftNum+1,totalNum=totalNum-1");

        return this.update(queryWrapper);
    }
}




