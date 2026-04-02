package redlib.backend.model;

import lombok.Data;

/**
 * 描述:admin_priv表的实体类（用户权限配置）
 * @version
 * @author:  系统
 * @创建时间: 2020-01-01
 */
@Data
public class AdminPriv {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 管理员ID（关联admin表）
     */
    private Integer adminId;

    /**
     * 模块ID（对应BackendModule注解的值）
     */
    private String modId;

    /**
     * 权限标识（对应Privilege注解的值）
     */
    private String priv;
}
