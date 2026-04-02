package redlib.backend.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 笔记查询条件对象
 *
 * @author wyyy
 * @date 2026-03-18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoteQueryDTO extends PageQueryDTO {
    /**
     * 用户ID（内部使用，用于数据隔离）
     */
    private Long userId;

    /**
     * 分类ID，用于筛选特定分类下的笔记
     */
    private Long categoryId;

    /**
     * 删除状态：null-查询所有，0-未删除，1-已删除
     */
    private Boolean isDeleted;

    /**
     * 是否管理员（root），用于 MyBatis 动态 SQL 区分全站查询
     */
    private Boolean isRoot;

    /**
     * 为 true 时仅查询收藏（is_starred = 1）；为 false 时仅非收藏；null 不筛选
     */
    private Boolean onlyStarred;

    /**
     * 标题模糊查询（管理员全站列表等场景）
     */
    private String title;

    /**
     * 按用户账号或姓名模糊匹配笔记所属用户（管理员全站列表；关联 admin 表）
     */
    private String ownerUserCode;

    /**
     * 当 {@link #ownerUserCode} 为纯数字时，同时按 admin 主键 id 精确匹配（由服务层填充）
     */
    private Integer ownerAdminId;

}
