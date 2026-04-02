package redlib.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员模块数据传输对象
 *
 * @author lihongwen
 * @date 2020/4/11
 */
@Data
public class AdminModDTO {
    /**
     * 模块ID
     */
    private String id;

    /**
     * 权限列表
     */
    private List<String> privList;
}
