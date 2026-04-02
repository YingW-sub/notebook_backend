package redlib.backend.vo;

import lombok.Data;
import redlib.backend.dto.CategoryDTO;

import java.util.Date;
import java.util.List;

/**
 * 笔记视图对象
 *
 * @author 18622
 * @date 2026-03-18
 */
@Data
public class NoteVO {
    /**
     * 笔记ID
     */
    private Long id;

    /**
     * 所属分类列表
     */
    private List<CategoryDTO> categories;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * AI提取的内容摘要
     */
    private String summary;

    /**
     * 是否星标收藏
     */
    private Boolean isStarred;

    /**
     * 是否置顶
     */
    private Boolean pinned;

    /**
     * 是否已删除
     */
    private Boolean deleted;

    /**
     * 删除时间
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
     * 笔记归属用户信息（仅管理员列表页填充）
     */
    private Long ownerUserId;
    private String ownerUserCode;
    private String ownerName;
}
