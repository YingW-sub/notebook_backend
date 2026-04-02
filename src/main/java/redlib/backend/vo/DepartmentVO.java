package redlib.backend.vo;

import lombok.Data;

import java.util.Date;

/**
 * 部门视图对象
 *
 * @author 李洪文
 * @date 2019/12/3 10:22
 */
@Data
public class DepartmentVO {
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

    /**
     * 创建日期
     */
    private Date createdAt;

    /**
     * 修改日期
     */
    private Date updatedAt;

    /**
     * 创建人ID
     */
    private Integer createdBy;

    /**
     * 创建人姓名
     */
    private String createdByDesc;
}
