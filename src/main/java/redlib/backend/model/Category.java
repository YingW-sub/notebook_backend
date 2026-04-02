package redlib.backend.model;

import java.util.Date;
import lombok.Data;

/**
 * 描述:category表的实体类（笔记分类）
 * @version
 * @author:  18622
 * @创建时间: 2026-03-17
 */
@Data
public class Category {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（数据隔离）
     */
    private Long userId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标记（0-未删除，1-已删除）
     */
    private Boolean deleted;

    // 手动添加 getter/setter 以确保 Lombok 抽风时也能编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
