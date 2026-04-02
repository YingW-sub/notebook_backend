package redlib.backend.dto;

import lombok.Data;

/**
 * 登录请求体（避免账号密码出现在 URL 查询参数中）
 */
@Data
public class LoginRequestDTO {
    private String userId;
    private String password;
}
