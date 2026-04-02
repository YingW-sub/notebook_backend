package redlib.backend.model;

import lombok.Data;

import java.util.Date;

/**
 * 部门实体对象
 *
 * @author 李洪文
 * @date 2019/12/3 10:38
 */
@Data
public class Department {
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
     * 创建时间
     */
    private Date createdAt;

    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 创建人ID
     */
    private Integer createdBy;

    /**
     * 更新人ID
     */
    private Integer updatedBy;

    /**
     * 删除标记
     */
    private Boolean deleted;
}
