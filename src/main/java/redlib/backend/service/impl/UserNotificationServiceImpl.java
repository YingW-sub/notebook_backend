package redlib.backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redlib.backend.dao.UserNotificationMapper;
import redlib.backend.dto.query.PageQueryDTO;
import redlib.backend.model.Page;
import redlib.backend.model.Token;
import redlib.backend.model.UserNotification;
import redlib.backend.service.UserNotificationService;
import redlib.backend.utils.PageUtils;
import redlib.backend.utils.ThreadContextHolder;
import redlib.backend.vo.UserNotificationVO;

@Service
public class UserNotificationServiceImpl implements UserNotificationService {

    @Autowired
    private UserNotificationMapper userNotificationMapper;

    private Token token() {
        return ThreadContextHolder.getToken();
    }

    @Override
    public int unreadCount() {
        return userNotificationMapper.countUnread(token().getUserId());
    }

    @Override
    public Page<UserNotificationVO> list(PageQueryDTO query) {
        if (query == null) {
            query = new PageQueryDTO();
        }
        int me = token().getUserId();
        int total = userNotificationMapper.countByUser(me);
        int c = query.getCurrent() > 0 ? query.getCurrent() : 1;
        int s = query.getPageSize() > 0 ? query.getPageSize() : 10;
        PageUtils pu = new PageUtils(c, s, total);
        if (total == 0) {
            return pu.getNullPage();
        }
        List<UserNotification> raw =
                userNotificationMapper.selectByUser(me, pu.getOffset(), pu.getLimit());
        List<UserNotificationVO> vos = new ArrayList<>(raw.size());
        for (UserNotification x : raw) {
            UserNotificationVO v = new UserNotificationVO();
            v.setId(x.getId());
            v.setCategory(x.getCategory());
            v.setTitle(x.getTitle());
            v.setBody(x.getBody());
            v.setRead(Boolean.TRUE.equals(x.getReadFlag()));
            v.setCreatedAt(x.getCreatedAt());
            vos.add(v);
        }
        return new Page<>(pu.getCurrent(), pu.getPageSize(), total, vos);
    }

    @Override
    public void markRead(long id) {
        userNotificationMapper.markRead(id, token().getUserId());
    }

    @Override
    public void markAllRead() {
        userNotificationMapper.markAllRead(token().getUserId());
    }
}
