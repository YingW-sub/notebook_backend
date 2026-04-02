package redlib.backend.service;

import redlib.backend.model.Token;
import redlib.backend.vo.OnlineUserVO;

import java.util.List;

/**
 * @author 李洪文
 * @date 2019/11/14 10:38
 */
public interface TokenService {
    /**
     * 用户登录，返回令牌信息
     *
     * @param userId   用户id
     * @param password 密码
     * @return 令牌信息
     */
    Token login(String userId, String password, String ipAddress, String userAgent);

    /**
     * 根据token获取令牌信息
     *
     * @param accessToken token
     * @return 令牌信息
     */
    Token getToken(String accessToken);

    /**
     * 登出系统
     *
     * @param accessToken 令牌token
     */
    void logout(String accessToken);


    /**
     * 获取在线用户列表
     *
     * @return
     */
    List<OnlineUserVO> list();

    /**
     * 将在线用户踢出系统
     *
     * @param accessToken 用户的accessToken
     */
    void kick(String accessToken);

    /**
     * 管理员权限或状态变更后，刷新该用户当前在线会话中的权限快照。
     * 若用户被禁用或不存在，则踢出其会话。
     *
     * @param userId 用户id
     */
    void refreshUserSessions(Integer userId);

    /**
     * 强制某用户所有在线会话失效（用于权限变更后强制重新登录）。
     *
     * @param userId 用户id
     */
    void invalidateUserSessions(Integer userId);
}
