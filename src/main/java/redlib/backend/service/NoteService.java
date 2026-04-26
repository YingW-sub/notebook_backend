package redlib.backend.service;

import org.springframework.web.multipart.MultipartFile;
import redlib.backend.dto.NoteDTO;
import redlib.backend.dto.NoteImportResultDTO;
import redlib.backend.dto.ReorderNotesDTO;
import redlib.backend.dto.query.NoteQueryDTO;
import redlib.backend.model.Page;
import redlib.backend.vo.NoteVO;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface NoteService {

    Page<NoteVO> listByPage(NoteQueryDTO queryDTO);
    // 添加笔记
    Long addNote(NoteDTO noteDTO);

    NoteDTO getById(Long id);
    // 获取笔记详情

    Long updateNote(NoteDTO noteDTO);
    // 收藏笔记
    void toggleFavorite(Long id);

    void deleteNote(Long id);
    // 删除笔记

    Page<NoteVO> listDeletedNotes(NoteQueryDTO queryDTO);
    // 恢复笔记
    void restoreNote(Long id);
    // 永久删除笔记
    void permanentDeleteNote(Long id);
    // 置顶笔记
    void togglePin(Long id);
    // 重新排序笔记
    void reorderNotes(ReorderNotesDTO body);

    // 导入文档
    NoteImportResultDTO importDocument(MultipartFile file);
    // 获取分类统计

    List<Map<String, Object>> getCategoryStatistics();
    // 导出 Word 文档
    void exportWord(Long id, OutputStream os);
    // 导出 PDF 文档
    void exportPdf(Long id, OutputStream os);

}
