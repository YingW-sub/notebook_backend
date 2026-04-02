package redlib.backend.dao;

import redlib.backend.model.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ChatMessage record);

    int insertSelective(ChatMessage record);

    ChatMessage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChatMessage record);

    int updateByPrimaryKeyWithBLOBs(ChatMessage record);

    int updateByPrimaryKey(ChatMessage record);

    List<ChatMessage> selectBySessionIdOrderByTime(@Param("userId") Long userId, @Param("sessionId") String sessionId, @Param("limit") Integer limit);

    List<ChatMessage> selectAllBySessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 按系统用户 ID 删除其全部聊天消息（删除 admin 时级联清理）
     */
    int deleteByUserIds(@Param("userIds") List<Integer> userIds);
}