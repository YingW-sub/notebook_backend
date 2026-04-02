package redlib.backend.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关键字查询条件对象
 *
 * @author lihongwen
 * @date 2020/3/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KeywordQueryDTO extends PageQueryDTO {
    /**
     * 关键字
     */
    private String keyword;

    /**
     * 排序字段
     */
    private String orderBy;
}
