package redlib.backend.service.impl;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import redlib.backend.dao.AdminMapper;
import redlib.backend.dao.AdminPrivMapper;
import redlib.backend.dao.LoginLogMapper;
import redlib.backend.model.Admin;
import redlib.backend.model.AdminPriv;
import redlib.backend.model.LoginLog;
import redlib.backend.model.Token;
import redlib.backend.service.TokenService;
import redlib.backend.service.utils.TokenUtils;
import redlib.backend.utils.FormatUtils;
import redlib.backend.vo.OnlineUserVO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private AdminPrivMapper adminPrivMapper;

    private Map<String, Token> tokenMap = new ConcurrentHashMap<>(1 << 8);

    /**
     * 用户登录，返回令牌信息
     *
     * @param userId    用户id
     * @param password  密码
     * @param ipAddress
     * @param userAgent
     * @return 令牌信息
     */
    @Override
    public Token login(String userId, String password, String ipAddress, String userAgent) {
        Admin admin = adminMapper.login(userId, FormatUtils.password(password));
        Assert.notNull(admin, "用户名或者密码错误");
        Assert.isTrue(Boolean.TRUE.equals(admin.getEnabled()), "此账户已经禁用，不能登录");
        Token token = new Token();
        token.setAccessToken(makeToken());
        token.setUserId(admin.getId());
        token.setLastAction(new Date());
        token.setDepartment(admin.getDepartment());
        token.setSex(admin.getSex());
        token.setIpAddress(ipAddress);
        token.setUserCode(userId);
        token.setUserName(admin.getName());
        token.setPrivSet(new HashSet<>());
        List<AdminPriv> privList = adminPrivMapper.list(admin.getId());
        token.setPrivSet(new HashSet<>());
        for (AdminPriv priv : privList) {
            token.getPrivSet().add(priv.getModId() + '.' + priv.getPriv());
        }
        try {
            UserAgent ua = UserAgent.parseUserAgentString(userAgent);
            Browser browser = ua.getBrowser();
            OperatingSystem os = ua.getOperatingSystem();
            Version version = ua.getBrowserVersion();
            if (browser != null) {
                token.setBrowser(browser.getName());
                if (version != null) {
                    token.setBrowser(token.getBrowser() + " V" + version.getVersion());
                }
            }

            if (os != null) {
                token.setOs(os.getName());
                if (os.getDeviceType() != null) {
                    token.setDevice(os.getDeviceType().getName());
                }
            }

            LoginLog loginLog = new LoginLog();
            loginLog.setName(token.getUserName());
            loginLog.setUserCode(token.getUserCode());
            loginLog.setIpAddress(token.getIpAddress());
            loginLog.setBrowser(token.getBrowser());
            loginLog.setOs(token.getOs());
            loginLogMapper.insert(loginLog);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 同一账号重复登录（例如权限变更后重新登录）时，移除该账号历史会话，避免在线用户出现重复人条目。
        Integer loginUserId = token.getUserId();
        String loginUserCode = token.getUserCode();
        if (loginUserId != null) {
            tokenMap.entrySet().removeIf(e -> {
                Token old = e.getValue();
                return (old != null
                        && old.getUserId() != null
                        && loginUserId.equals(old.getUserId())
                        && !token.getAccessToken().equals(e.getKey()))
                        || (old != null
                        && old.getUserCode() != null
                        && loginUserCode != null
                        && loginUserCode.equalsIgnoreCase(old.getUserCode())
                        && !token.getAccessToken().equals(e.getKey()));
            });
        }
        tokenMap.put(token.getAccessToken(), token);
        return token;
    }

    /**
     * 根据token获取令牌信息
     *
     * @param accessToken token
     * @return 令牌信息
     */
    @Override
    public Token getToken(String accessToken) {
        if (FormatUtils.isEmpty(accessToken)) {
            return null;
        }

        return tokenMap.get(accessToken);
    }

    /**
     * 登出系统
     *
     * @param accessToken 令牌token
     */
    @Override
    public void logout(String accessToken) {
        tokenMap.remove(accessToken);
    }

    /**
     * 获取在线用户列表
     *
     * @return
     */
    @Override
    public List<OnlineUserVO> list() {
        // 统一会话去重：同一人（优先 userId，其次 userCode）仅保留最后活跃会话，避免权限调整后出现重复在线用户。
        Map<String, Map.Entry<String, Token>> latestByUser = new HashMap<>();
        for (Map.Entry<String, Token> e : tokenMap.entrySet()) {
            Token t = e.getValue();
            if (t == null) {
                continue;
            }
            String key = onlineUserKey(t);
            if (key == null) {
                continue;
            }
            Map.Entry<String, Token> prevEntry = latestByUser.get(key);
            if (prevEntry == null) {
                latestByUser.put(key, e);
                continue;
            }
            Token prev = prevEntry.getValue();
            Date p = prev != null ? prev.getLastAction() : null;
            Date c = t.getLastAction();
            if (p == null || (c != null && c.after(p))) {
                latestByUser.put(key, e);
            }
        }

        // 压缩 tokenMap，删除同一人的历史会话，确保后续在线列表与踢人行为一致
        Set<String> keepTokenSet = latestByUser.values().stream()
                .map(Map.Entry::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!keepTokenSet.isEmpty()) {
            tokenMap.entrySet().removeIf(e -> !keepTokenSet.contains(e.getKey()));
        }

        return latestByUser.values().stream()
                .map(Map.Entry::getValue)
                .map(item -> {
                    OnlineUserVO vo = TokenUtils.convertToVO(item);
                    if (item.getUserId() != null) {
                        Admin admin = adminMapper.selectByPrimaryKey(item.getUserId());
                        if (admin != null) {
                            vo.setEnabled(admin.getEnabled());
                            vo.setSex(admin.getSex());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private String onlineUserKey(Token token) {
        if (token == null) {
            return null;
        }
        if (token.getUserId() != null) {
            return "id:" + token.getUserId();
        }
        if (token.getUserCode() != null && !token.getUserCode().isBlank()) {
            return "uc:" + token.getUserCode().trim().toLowerCase(Locale.ROOT);
        }
        return null;
    }

    /**
     * 将在线用户踢出系统
     *
     * @param accessToken 用户的accessToken
     */
    @Override
    public void kick(String accessToken) {
        tokenMap.remove(accessToken);
    }

    @Override
    public void refreshUserSessions(Integer userId) {
        if (userId == null) {
            return;
        }
        Admin admin = adminMapper.selectByPrimaryKey(userId);
        if (admin == null || !Boolean.TRUE.equals(admin.getEnabled())) {
            tokenMap.entrySet().removeIf(e -> {
                Token t = e.getValue();
                return t != null && userId.equals(t.getUserId());
            });
            return;
        }
        Set<String> privSet = new HashSet<>();
        List<AdminPriv> privList = adminPrivMapper.list(userId);
        for (AdminPriv priv : privList) {
            privSet.add(priv.getModId() + "." + priv.getPriv());
        }
        tokenMap.forEach((k, t) -> {
            if (t == null || !userId.equals(t.getUserId())) {
                return;
            }
            t.setUserCode(admin.getUserCode());
            t.setUserName(admin.getName());
            t.setDepartment(admin.getDepartment());
            t.setSex(admin.getSex());
            t.setPrivSet(new HashSet<>(privSet));
        });
    }

    @Override
    public void invalidateUserSessions(Integer userId) {
        if (userId == null) {
            return;
        }
        tokenMap.entrySet().removeIf(e -> {
            Token t = e.getValue();
            return t != null && userId.equals(t.getUserId());
        });
    }

    private String makeToken() {
        return UUID.randomUUID().toString().replaceAll("-", "") + "";
    }
}
