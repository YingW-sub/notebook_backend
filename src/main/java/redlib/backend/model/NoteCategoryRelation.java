package redlib.backend.model;

import java.util.Date;
import lombok.Data;

/**
 * 笔记与分类多对多关联实体类
 *
 * @author 18622
 * @date 2026-03-22
 */
@Data
public class NoteCategoryRelation {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 创建时间
     */
    private Date createTime;
}
