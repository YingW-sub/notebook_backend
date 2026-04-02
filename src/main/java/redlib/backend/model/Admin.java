package redlib.backend.model;

import lombok.Data;

import java.util.Date;

/**
 * 描述:admin表的实体类（系统用户/管理员）
 * @version
 * @author:  系统
 * @创建时间: 2020-01-01
 */
@Data
public class Admin {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户代码（登录账号）
     */
    private String userCode;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 性别（0-女，1-男）
     */
    private Integer sex;

    /**
     * 是否启用（true-启用，false-禁用）
     */
    private Boolean enabled;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 部门
     */
    private String department;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 创建人ID
     */
    private Integer createdBy;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 更新人ID
     */
    private Integer updatedBy;
}
