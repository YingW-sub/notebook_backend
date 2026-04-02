package redlib.backend.vo;

import lombok.Data;

import java.util.Date;

/**
 * 管理员视图对象
 *
 * @author lihongwen
 * @date 2020/4/8
 */
@Data
public class AdminVO {
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
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 密码（仅用于传输，不展示）
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
     * 创建人姓名
     */
    private String createdByDesc;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 更新人ID
     */
    private Integer updatedBy;

    /**
     * 更新人姓名
     */
    private String updatedByDesc;
}
