package redlib.backend.model;

import lombok.Data;

/**
 * 描述:统一API响应数据结构
 * @version
 * @author:  liuweis
 * @date: 2020/8/31
 */
@Data
public class ResponseData<T> {
    /**
     * 状态码（200-成功，其他-失败）
     */
    private Integer code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 是否成功
     */
    private Boolean success;
}
