package redlib.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 笔记数据传输对象
 *
 * @author 18622
 * @date 2026-03-18
 */
@Data
public class NoteDTO {

    /**
     * 笔记ID
     */
    private Long id;

    /**
     * 分类ID列表（支持多分类）
     */
    private List<Long> categoryIds;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记正文内容
     */
    private String content;

    /**
     * AI提取的内容摘要
     */
    private String summary;

}
