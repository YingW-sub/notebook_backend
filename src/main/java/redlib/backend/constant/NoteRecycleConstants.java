package redlib.backend.constant;

/**
 * 回收站软删保留天数，超时后定时任务物理删除且不可恢复。
 */
public final class NoteRecycleConstants {

    public static final int RETENTION_DAYS = 15;

    private NoteRecycleConstants() {
    }
}
