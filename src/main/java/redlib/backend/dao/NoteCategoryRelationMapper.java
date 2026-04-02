package redlib.backend.dao;

import org.apache.ibatis.annotations.Param;
import redlib.backend.model.NoteCategoryRelation;

import java.util.List;

/**
 * 笔记与分类多对多关联 Mapper 接口
 *
 * @author 18622
 * @date 2026-03-22
 */
public interface NoteCategoryRelationMapper {

    /**
     * 批量插入笔记与分类的关联关系
     *
     * @param relations 关联关系列表
     */
    void batchInsert(@Param("relations") List<NoteCategoryRelation> relations);

    /**
     * 根据笔记ID删除所有关联关系
     *
     * @param noteId 笔记ID
     */
    void deleteByNoteId(@Param("noteId") Long noteId);

    /**
     * 根据笔记ID查询所有关联的分类ID
     *
     * @param noteId 笔记ID
     * @return 分类ID列表
     */
    List<Long> selectCategoryIdsByNoteId(@Param("noteId") Long noteId);

    /**
     * 根据笔记ID列表批量查询分类ID
     *
     * @param noteIds 笔记ID列表
     * @return 每个笔记ID对应的分类ID列表（Map结构）
     */
    List<NoteCategoryRelation> selectByNoteIds(@Param("noteIds") List<Long> noteIds);

    /**
     * 按笔记 ID 列表批量删除关联（删除用户前清理）
     */
    void deleteByNoteIds(@Param("noteIds") List<Long> noteIds);
}
