package redlib.backend.dao;

import org.apache.ibatis.annotations.Param;
import redlib.backend.model.Category;

import java.util.List;

public interface CategoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    /**
     * 根据用户ID查询分类列表
     *
     * @param userId 用户ID
     * @return 分类列表
     */
    List<Category> selectByUserId(Long userId);

    /**
     * 根据分类ID列表查询未被软删除的分类
     *
     * @param ids 分类ID列表
     * @return 分类列表
     */
    List<Category> selectActiveByIds(@Param("ids") List<Long> ids);

    /**
     * 根据用户ID和分类名称查询，用于分类名称防重校验
     *
     * @param userId       用户ID
     * @param categoryName 分类名称
     * @return 分类记录
     */
    Category selectByUserIdAndName(@Param("userId") Long userId, @Param("categoryName") String categoryName);

    /**
     * 按系统用户 ID 物理删除其全部分类（删除 admin 时级联清理）
     */
    int deleteByUserIds(@Param("userIds") List<Integer> userIds);
}