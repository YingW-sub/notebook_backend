package redlib.backend.model;

import java.util.Date;
import lombok.Data;

/**
 * 描述:login_log表的实体类（登录日志）
 * @version
 * @author:  stone
 * @创建时间: 2023-01-11
 */
@Data
public class LoginLog {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户代码
     */
    private String userCode;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 创建时间
     */
    private Date createdAt;
}
