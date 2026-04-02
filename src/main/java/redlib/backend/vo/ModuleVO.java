package redlib.backend.vo;

import lombok.Data;

import java.util.List;

/**
 * 模块视图对象（包含模块ID和权限列表）
 *
 * @author lihongwen
 * @date 2020/3/17
 */
@Data
public class ModuleVO {
    /**
     * 模块ID
     */
    private String id;

    /**
     * 权限列表
     */
    private List<PrivilegeVO> privilegeList;
}
