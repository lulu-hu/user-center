package com.lulu.usercenter.Model.vo;



import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 *  用户信息封装类
 * @author lulu
 * @date 2022/11/18 13:47
 */
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = -246523005855501866L;

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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     *  创建人用户信息
     */
    UserVO createUser;

}
