package redlib.backend.service;

import redlib.backend.model.Page;
import redlib.backend.dto.query.PageQueryDTO;
import redlib.backend.vo.UserNotificationVO;

public interface UserNotificationService {

    int unreadCount();

    Page<UserNotificationVO> list(PageQueryDTO query);

    void markRead(long id);

    void markAllRead();
}
