package redlib.backend.vo;

import lombok.Data;

import java.util.Date;

/**
 * 在线用户视图对象
 *
 * @author lihongwen
 * @date 2020/4/1
 */
@Data
public class OnlineUserVO {
    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 是否具有访问后台管理的权限
     */
    private boolean backend;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户代码
     */
    private String userCode;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 最后操作时间
     */
    private Date lastAction;

    /**
     * 性别（0-女，1-男），与 admin 表一致
     */
    private Integer sex;

    /**
     * 账户是否启用（与「用户管理」一致）
     */
    private Boolean enabled;

    /**
     * 部门
     */
    private String department;

    /**
     * IP地址
     */
    private String ipAddr;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 浏览器版本
     */
    private String browserVersion;

    /**
     * 设备类型
     */
    private String device;

    /**
     * 国家
     */
    private String country;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 运营商
     */
    private String isp;

    /**
     * 总流量
     */
    private Long totalNetFlow;

    /**
     * 来源页面
     */
    private String referer;
}
