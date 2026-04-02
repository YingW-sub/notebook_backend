package redlib.backend.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import redlib.backend.model.LoginLog;

/**
 * 登录日志视图对象
 *
 * @author lihongwen
 * @date 2020/4/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginLogVO extends LoginLog {
}
