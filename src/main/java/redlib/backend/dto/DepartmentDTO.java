package redlib.backend.dto;

import lombok.Data;

/**
 * 部门数据传输对象
 *
 * @author 李洪文
 * @date 2019/12/3 9:20
 */
@Data
public class DepartmentDTO {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 描述
     */
    private String description;
}
