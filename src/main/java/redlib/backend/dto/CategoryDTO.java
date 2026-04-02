package redlib.backend.dto;

import lombok.Data;

/**
 * 分类数据传输对象
 *
 * @author 18622
 * @date 2026-03-17
 */
@Data
public class CategoryDTO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;
}
