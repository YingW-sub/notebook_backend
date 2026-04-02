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

    Long addNote(NoteDTO noteDTO);

    NoteDTO getById(Long id);

    Long updateNote(NoteDTO noteDTO);

    void toggleFavorite(Long id);

    void deleteNote(Long id);

    Page<NoteVO> listDeletedNotes(NoteQueryDTO queryDTO);

    void restoreNote(Long id);

    void permanentDeleteNote(Long id);

    void togglePin(Long id);

    void reorderNotes(ReorderNotesDTO body);

    NoteImportResultDTO importDocument(MultipartFile file);

    List<Map<String, Object>> getCategoryStatistics();

    void exportWord(Long id, OutputStream os);

    void exportPdf(Long id, OutputStream os);

}
