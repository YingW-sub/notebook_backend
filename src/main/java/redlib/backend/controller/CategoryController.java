package redlib.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redlib.backend.annotation.BackendModule;
import redlib.backend.annotation.Privilege;
import redlib.backend.dto.CategoryDTO;
import redlib.backend.dto.JsonIdRequest;
import redlib.backend.service.CategoryService;

import java.util.List;

/**
 * 分类管理后端服务模块
 *
 * @author 18622
 * @date 2026-03-17
 */

@RestController
@RequestMapping("/api/category")
@BackendModule({"page:页面", "update:修改", "add:创建", "delete:删除"})
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取当前用户的所有分类列表
     */
    @GetMapping("listCategory")
    @Privilege("page")
    public List<CategoryDTO> listCategory() {
        return categoryService.listAllCategories();
    }

    /**
     * 添加分类
     */
    @PostMapping("addCategory")
    @Privilege("add")
    public Long addCategory(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.addCategory(categoryDTO);
    }

    /**
     * 修改分类名称
     */
    @PostMapping("updateCategory")
    @Privilege("update")
    public Long updateCategory(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.updateCategory(categoryDTO);
    }

    /**
     * 删除分类（软删除）
     */
    @PostMapping("deleteCategory")
    @Privilege("delete")
    public void deleteCategory(@RequestBody JsonIdRequest body) {
        categoryService.deleteCategory(body.getId());
    }
}
