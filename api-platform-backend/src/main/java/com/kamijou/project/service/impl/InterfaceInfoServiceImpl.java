package com.kamijou.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kamijou.apicommon.model.entity.InterfaceInfo;
import com.kamijou.project.common.ErrorCode;
import com.kamijou.project.exception.BusinessException;
import com.kamijou.project.mapper.InterfaceInfoMapper;
import com.kamijou.project.service.InterfaceInfoService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author kamijou
 * @description 针对表【interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2024-03-24 09:58:40
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = interfaceInfo.getName();
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String description = interfaceInfo.getDescription();


        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name) || ObjectUtils.anyNull(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

}




