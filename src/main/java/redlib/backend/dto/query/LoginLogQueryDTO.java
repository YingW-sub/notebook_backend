package redlib.backend.dto.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 登录日志查询条件对象
 *
 * @author lihongwen
 * @date 2020/4/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginLogQueryDTO extends PageQueryDTO {
    /**
     * 用户代码
     */
    private String userCode;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 创建日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    /**
     * 排序字段
     */
    private String orderBy;
}
