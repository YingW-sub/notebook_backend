package redlib.backend.model;

import java.util.Date;
import lombok.Data;

/**
 * 描述:note表的实体类
 *
 * @version
 * @author:  18622
 * @创建时间: 2026-03-17
 */
@Data
public class Note {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（数据隔离）
     */
    private Long userId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * AI提取的内容摘要
     */
    private String summary;

    /**
     * 是否是星标收藏，0-否，1-是
     */
    private Boolean isStarred;

    /**
     * 是否置顶
     */
    private Boolean pinned;

    /**
     * 同用户内排序序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 删除标记，0-未删除，1-已删除进入回收站
     */
    private Boolean deleted;

    /**
     * 删除时间（用于30天自动清理）
     */
    private Date deleteTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 笔记正文内容
     */
    private String content;
}
