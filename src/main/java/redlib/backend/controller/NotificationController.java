package redlib.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redlib.backend.annotation.BackendModule;
import redlib.backend.annotation.Privilege;
import redlib.backend.dto.JsonIdRequest;
import redlib.backend.dto.query.PageQueryDTO;
import redlib.backend.model.Page;
import redlib.backend.service.UserNotificationService;
import redlib.backend.vo.UserNotificationVO;

@RestController
@RequestMapping("/api/notification")
@BackendModule({"page:页面"})
public class NotificationController {

    @Autowired
    private UserNotificationService userNotificationService;

    @GetMapping("unreadCount")
    @Privilege
    public int unreadCount() {
        return userNotificationService.unreadCount();
    }

    @PostMapping("list")
    @Privilege
    public Page<UserNotificationVO> list(@RequestBody PageQueryDTO query) {
        return userNotificationService.list(query);
    }

    @PostMapping("markRead")
    @Privilege
    public void markRead(@RequestBody JsonIdRequest body) {
        Assert.notNull(body.getId(), "id不能为空");
        userNotificationService.markRead(body.getId());
    }

    /** 无需请求体，避免前端发空 JSON 被拦截 */
    @PostMapping("markAllRead")
    @Privilege
    public void markAllRead() {
        userNotificationService.markAllRead();
    }
}
