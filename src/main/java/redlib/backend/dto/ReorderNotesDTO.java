package redlib.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 笔记排序请求体：拖拽后的笔记 ID 有序列表
 *
 * @author qzy
 */
@Data
public class ReorderNotesDTO {
    /**
     * 笔记 ID 有序列表（拖拽后的顺序）
     */
    private List<Long> orderedIds;
}
