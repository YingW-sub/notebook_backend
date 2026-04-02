package redlib.backend.dao;
import org.apache.ibatis.annotations.Param;
import redlib.backend.dto.query.NoteQueryDTO;
import redlib.backend.model.Note;

import java.util.List;
import java.util.Map;

public interface NoteMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Note record);

    int insertSelective(Note record);

    Note selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Note record);

    int updateByPrimaryKeyWithBLOBs(Note record);

    int updateByPrimaryKey(Note record);

    /**
     * 分页查询笔记列表
     *
     * @param queryDTO 查询条件
     * @param offset   偏移量
     * @param limit    每页条数
     * @return 笔记列表
     */
    List<Note> list(@Param("queryDTO") NoteQueryDTO queryDTO, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计笔记总数
     *
     * @param queryDTO 查询条件
     * @return 笔记总数
     */
    Integer count(@Param("queryDTO") NoteQueryDTO queryDTO);

    /**
     * 统计各分类的笔记数量（用于ECharts图表）
     *
     * @param userId 用户ID
     * @return 分类名称和笔记数量的映射列表
     */
    List<Map<String, Object>> countByCategory(Long userId);

    /**
     * 查询超过指定天数且已删除的笔记
     *
     * @param days 天数
     * @return 超过指定天数且已删除的笔记列表
     */
    List<Note> selectDeletedNotesOlderThanDays(Integer days);

    /**
     * 批量查询笔记对应的分类ID（Map结构: noteId -> categoryId）
     *
     * @param noteIds 笔记ID列表
     * @return Map列表
     */
    List<Map<String, Object>> selectNoteCategories(@Param("noteIds") List<Long> noteIds);

    /**
     * 查询指定系统用户（admin.id）名下的全部笔记主键（删除用户前级联清理）
     */
    List<Long> selectIdsByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * 按系统用户 ID 物理删除其全部笔记
     */
    int deleteByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * 统计各用户在 admin 表中仍存在的笔记数量（管理员系统活跃度统计用），取 Top N
     */
    List<Map<String, Object>> countNotesByUser(@Param("topN") int topN);

    /**
     * 全站回收站笔记数量（未删除用户对应的软删笔记）
     */
    Integer countGlobalDeletedNotes();

    /**
     * 全站有效笔记总数（未删除且 user_id 在 admin 中存在）
     */
    Integer countGlobalActiveNotes();

    Integer selectMaxSortOrderByUser(@Param("userId") Long userId);

    List<Long> selectIdsByUserNonDeleted(@Param("userId") Long userId);

}