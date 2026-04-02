package redlib.backend.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import redlib.backend.model.UserNotification;

public interface UserNotificationMapper {

    int insert(UserNotification row);

    int countUnread(@Param("userId") int userId);

    int countByUser(@Param("userId") int userId);

    List<UserNotification> selectByUser(
            @Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    int markRead(@Param("id") long id, @Param("userId") int userId);

    int markAllRead(@Param("userId") int userId);
}
