package redlib.backend.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import redlib.backend.dao.CategoryMapper;
import redlib.backend.dto.CategoryDTO;
import redlib.backend.model.Category;
import redlib.backend.service.CategoryService;
import redlib.backend.utils.ThreadContextHolder;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类模块服务实现类
 *
 * @author 18622
 * @date 2026-03-17
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加分类
     * 从ThreadContextHolder获取当前登录用户的user_id，确保数据隔离
     */
    @Override
    public Long addCategory(CategoryDTO categoryDTO) {
        // 从当前上下文获取用户ID，确保数据隔离
        Long userId = ThreadContextHolder.getToken().getUserId().longValue();

        Assert.hasText(categoryDTO.getCategoryName(), "分类名称不能为空");

        // 校验同名分类（同一用户下不能有重复的未删除分类名称）
        Category existing = categoryMapper.selectByUserIdAndName(userId, categoryDTO.getCategoryName());
        Assert.isNull(existing, "该分类名称已存在");

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUserId(userId);
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        category.setDeleted(false);

        categoryMapper.insert(category);
        return category.getId();
    }

    /**
     * 删除分类（软删除）
     * 确保只能删除当前用户自己的分类
     */
    @Override
    public void deleteCategory(Long id) {
        Long userId = ThreadContextHolder.getToken().getUserId().longValue();

        Assert.notNull(id, "分类ID不能为空");

        // 查询分类是否存在且属于当前用户
        Category category = categoryMapper.selectByPrimaryKey(id);
        Assert.notNull(category, "分类不存在");
        Assert.isTrue(category.getUserId().equals(userId), "无权限操作他人的分类");

        // 软删除：更新deleted标记
        category.setDeleted(true);
        category.setUpdateTime(new Date());
        categoryMapper.updateByPrimaryKey(category);
    }

    /**
     * 修改分类名称
     * 确保只能修改当前用户自己的分类
     */
    @Override
    public Long updateCategory(CategoryDTO categoryDTO) {
        Long userId = ThreadContextHolder.getToken().getUserId().longValue();

        Assert.notNull(categoryDTO.getId(), "分类ID不能为空");
        Assert.hasText(categoryDTO.getCategoryName(), "分类名称不能为空");

        // 查询分类是否存在且属于当前用户
        Category category = categoryMapper.selectByPrimaryKey(categoryDTO.getId());
        Assert.notNull(category, "分类不存在");
        Assert.isTrue(category.getUserId().equals(userId), "无权限操作他人的分类");

        // 更新分类名称
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setUpdateTime(new Date());
        categoryMapper.updateByPrimaryKey(category);

        return category.getId();
    }

    /**
     * 查询当前用户的所有分类
     * 仅返回当前用户创建的分类，确保数据隔离
     */
    @Override
    public List<CategoryDTO> listAllCategories() {
        // 从当前上下文获取用户ID，确保数据隔离
        Long userId = ThreadContextHolder.getToken().getUserId().longValue();

        List<Category> categories = categoryMapper.selectByUserId(userId);

        return categories.stream()
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(category.getId());
                    dto.setCategoryName(category.getCategoryName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
