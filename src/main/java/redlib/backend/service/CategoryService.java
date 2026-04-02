package redlib.backend.service;

import redlib.backend.dto.CategoryDTO;

import java.util.List;

/**
 * 分类模块服务接口
 *
 * @author 18622
 * @date 2026-03-17
 */
public interface CategoryService {

    /**
     * 添加分类
     *
     * @param categoryDTO 分类输入对象
     * @return 分类ID
     */
    Long addCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类（软删除）
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 修改分类名称
     *
     * @param categoryDTO 分类输入对象（包含ID和新名称）
     * @return 分类ID
     */
    Long updateCategory(CategoryDTO categoryDTO);

    /**
     * 查询当前用户的所有分类
     *
     * @return 分类列表
     */
    List<CategoryDTO> listAllCategories();
}
