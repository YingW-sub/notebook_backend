package redlib.backend.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门查询条件对象
 *
 * @author 李洪文
 * @date 2019/12/3 10:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DepartmentQueryDTO extends PageQueryDTO {
    /**
     * 部门名称（模糊查询）
     */
    private String departmentName;
}
