package redlib.backend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlib.backend.annotation.BackendModule;
import redlib.backend.dto.LoginRequestDTO;
import redlib.backend.annotation.NeedNoPrivilege;
import redlib.backend.annotation.Privilege;
import redlib.backend.model.Token;
import redlib.backend.service.TokenService;
import redlib.backend.utils.ThreadContextHolder;

@RestController
@RequestMapping("/api/auth")
@BackendModule({"page:页面"})
public class AuthenticationController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("login")
    @NeedNoPrivilege
    public Token login(
            @RequestBody LoginRequestDTO body,
            HttpServletRequest request,
            HttpServletResponse response) {
        Assert.notNull(body, "请求体不能为空");
        String userId = body.getUserId();
        String password = body.getPassword();
        Assert.hasText(userId, "用户名不能为空");
        Assert.hasText(password, "密码不能为空");
        String ipAddress = request.getRemoteAddr();
        ipAddress = ipAddress.replace("[", "").replace("]", "");
        Token token = tokenService.login(userId, password, ipAddress, request.getHeader("user-agent"));
        Cookie cookie = new Cookie("accessToken", token.getAccessToken());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return token;
    }

    @GetMapping("getCurrentUser")
    @Privilege
    public Token getCurrentUser() {
        return ThreadContextHolder.getToken();
    }

    @GetMapping("logout")
    @Privilege
    public void logout() {
        Token token = null;
        try {
            token = ThreadContextHolder.getToken();
        } catch (Exception ignore) {
        }
        if (token != null) {
            tokenService.logout(token.getAccessToken());
        }
    }

    @GetMapping("ping")
    @Privilege
    public void ping() {
    }
}
