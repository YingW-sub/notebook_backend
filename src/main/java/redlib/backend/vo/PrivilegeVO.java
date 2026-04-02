package redlib.backend.vo;

import lombok.Data;

/**
 * 权限视图对象
 *
 * @author lihongwen
 * @date 2020/3/17
 */
@Data
public class PrivilegeVO {
    /**
     * 权限ID
     */
    private String id;

    /**
     * 权限描述
     */
    private String description;
}
