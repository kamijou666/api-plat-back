package com.kamijou.project.model.vo;

import com.kamijou.project.model.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口信息封装视图
 *
 * @author kamijou
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVO extends Post {

    /**
     * 调用次数
     */
    private Integer totalNum;
    /**
     * 调用次数
     */
    private String name;

    private static final long serialVersionUID = 1L;
}