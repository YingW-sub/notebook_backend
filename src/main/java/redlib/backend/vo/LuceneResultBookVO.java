package redlib.backend.vo;

import lombok.Data;

import java.util.List;

/**
 * Lucene搜索结果视图对象
 *
 * @author 系统
 * @date 2020-01-01
 */
@Data
public class LuceneResultBookVO {
    /**
     * 书籍ID
     */
    private Integer bookId;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 相关性得分
     */
    private Double score;

    /**
     * 高亮片段列表
     */
    private List<String> highlights;
}
