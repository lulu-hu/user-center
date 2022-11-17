package com.lulu.usercenter.Model.dto;

import com.lulu.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 *  查询封装类
 * @author lulu
 * @date 2022/11/17 13:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}
