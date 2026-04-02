package redlib.backend.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redlib.backend.constant.NoteRecycleConstants;
import redlib.backend.dao.NoteCategoryRelationMapper;
import redlib.backend.dao.NoteMapper;
import redlib.backend.model.Note;

import java.util.List;

@Component
@Slf4j
public class NoteCleanupTask {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NoteCategoryRelationMapper relationMapper;

    /**
     * 每天凌晨 2 点：物理删除超过保留期的软删笔记
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupDeletedNotes() {
        log.info("开始执行笔记定时清理任务...");

        List<Note> deletedNotes = noteMapper.selectDeletedNotesOlderThanDays(NoteRecycleConstants.RETENTION_DAYS);

        if (deletedNotes.isEmpty()) {
            log.info("没有需要清理的笔记记录");
            return;
        }

        int deletedCount = 0;
        for (Note note : deletedNotes) {
            relationMapper.deleteByNoteId(note.getId());
            noteMapper.deleteByPrimaryKey(note.getId());
            deletedCount++;
            log.debug("物理删除笔记ID: {}", note.getId());
        }

        log.info("笔记定时清理任务完成，共清理 {} 条记录", deletedCount);
    }
}
