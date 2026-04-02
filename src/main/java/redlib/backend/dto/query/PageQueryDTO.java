package redlib.backend.dto.query;

import lombok.Data;

/**
 * 分页查询基类
 *
 * @author lihongwen
 * @date 2020/3/17
 */
@Data
public class PageQueryDTO {
    /**
     * 当前页码
     */
    private int current;

    /**
     * 每页记录数
     */
    private int pageSize;
}
