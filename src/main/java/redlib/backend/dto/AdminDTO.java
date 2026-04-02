package redlib.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员数据传输对象
 *
 * @author lihongwen
 * @date 2020/4/11
 */
@Data
public class AdminDTO {
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
     * 密码（加密传输）
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
     * 权限列表
     */
    private List<AdminModDTO> modList;
}
