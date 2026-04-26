package redlib.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import redlib.backend.annotation.BackendModule;
import redlib.backend.annotation.Privilege;
import redlib.backend.dto.JsonIdRequest;
import redlib.backend.dto.NoteDTO;
import redlib.backend.dto.NoteImportResultDTO;
import redlib.backend.dto.ReorderNotesDTO;
import redlib.backend.dto.query.NoteQueryDTO;
import redlib.backend.model.Page;
import redlib.backend.service.NoteService;
import redlib.backend.vo.NoteVO;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/note")
@BackendModule({"page:页面", "update:修改", "add:创建", "delete:删除"})
public class NoteController {

    @Autowired
    private NoteService noteService;
    //获取笔记列表
    @PostMapping("listNote")
    @Privilege("page")
    public Page<NoteVO> listNote(@RequestBody NoteQueryDTO queryDTO) {
        return noteService.listByPage(queryDTO);
    }

    //添加笔记
    @PostMapping("addNote")
    @Privilege("add")
    public Long addNote(@RequestBody NoteDTO noteDTO) {
        return noteService.addNote(noteDTO);
    }

    //获取笔记详情
    @GetMapping("getNote")
    @Privilege("page")
    public NoteDTO getNote(Long id) {
        return noteService.getById(id);
    }
    //更新笔记
    @PostMapping("updateNote")
    @Privilege("update")
    public Long updateNote(@RequestBody NoteDTO noteDTO) {
        return noteService.updateNote(noteDTO);
    }
    //收藏笔记
    @PostMapping("toggleFavorite")
    @Privilege("update")
    public void toggleFavorite(@RequestBody JsonIdRequest body) {
        noteService.toggleFavorite(body.getId());
    }

    //置顶笔记
    @PostMapping("togglePin")
    @Privilege("update")
    public void togglePin(@RequestBody JsonIdRequest body) {
        noteService.togglePin(body.getId());
    }

    //重新排序笔记
    @PostMapping("reorderNotes")
    @Privilege("update")
    public void reorderNotes(@RequestBody ReorderNotesDTO body) {
        noteService.reorderNotes(body);
    }

    //导入文档
    @PostMapping(value = "importDocument", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Privilege("add")
    public NoteImportResultDTO importDocument(@RequestPart("file") MultipartFile file) {
        return noteService.importDocument(file);
    }

    //删除笔记
    @PostMapping("deleteNote")
    @Privilege("delete")
    public void deleteNote(@RequestBody JsonIdRequest body) {
        noteService.deleteNote(body.getId());
    }

   
    @PostMapping("listDeletedNotes")
    @Privilege("page")
    public Page<NoteVO> listDeletedNotes(@RequestBody NoteQueryDTO queryDTO) {
        return noteService.listDeletedNotes(queryDTO);
    }

    //恢复笔记
    @PostMapping("restoreNote")
    @Privilege("update")
    public void restoreNote(@RequestBody JsonIdRequest body) {
        noteService.restoreNote(body.getId());
    }

    //永久删除笔记
    @PostMapping("permanentDeleteNote")
    @Privilege("delete")
    public void permanentDeleteNote(@RequestBody JsonIdRequest body) {
        noteService.permanentDeleteNote(body.getId());
    }

    //获取分类统计
    @GetMapping("getCategoryStatistics")
    @Privilege("page")
    public List<Map<String, Object>> getCategoryStatistics() {
        return noteService.getCategoryStatistics();
    }

    //设置内容disposition响应头
    private void setContentDisposition(HttpServletResponse response, long noteId, String utf8FileName) {
        int dot = utf8FileName.lastIndexOf('.');
        String ext = dot >= 0 ? utf8FileName.substring(dot) : "";
        String asciiFallback = "note-" + noteId + ext;
        String encoded = URLEncoder.encode(utf8FileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encoded);
    }
//导出笔记（.md .html）
    @GetMapping("exportNote")
    @Privilege("page")
    public void exportNote(Long id, HttpServletResponse response) throws IOException {
        NoteDTO note = noteService.getById(id);
        if (note == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter writer = response.getWriter();
            writer.write("笔记不存在");
            writer.flush();
            return;
        }

        String title = note.getTitle() != null ? note.getTitle() : "无标题";
        String content = note.getContent() != null ? note.getContent() : "";

        boolean html = content.trim().startsWith("<");
        String fileName;
        String bodyOut;
        if (html) {
            fileName = title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".html";
            bodyOut = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>"
                    + escapeHtml(title) + "</title></head><body>"
                    + content + "</body></html>";
            response.setContentType("text/html;charset=UTF-8");
        } else {
            fileName = title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".md";
            StringBuilder markdown = new StringBuilder();
            markdown.append("# ").append(title).append("\n\n");
            if (note.getSummary() != null && !note.getSummary().isBlank()) {
                markdown.append("> 摘要：").append(note.getSummary()).append("\n\n");
            }
            markdown.append(content);
            bodyOut = markdown.toString();
            response.setContentType("text/markdown;charset=UTF-8");
        }
        setContentDisposition(response, id, fileName);
        response.getWriter().write(bodyOut);
        response.getWriter().flush();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
    //导出Word文档
    @GetMapping("exportWord")
    @Privilege("page")
    public void exportWord(Long id, HttpServletResponse response) throws IOException {
        NoteDTO note = noteService.getById(id);
        if (note == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter writer = response.getWriter();
            writer.write("笔记不存在");
            writer.flush();
            return;
        }

        String title = note.getTitle() != null ? note.getTitle() : "无标题";
        String fileName = title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".docx";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            noteService.exportWord(id, baos);
        } catch (RuntimeException e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            response.getWriter().flush();
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        setContentDisposition(response, id, fileName);
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }

    //导出PDF文档
    @GetMapping("exportPdf")
    @Privilege("page")
    public void exportPdf(Long id, HttpServletResponse response) throws IOException {
        NoteDTO note = noteService.getById(id);
        if (note == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter writer = response.getWriter();
            writer.write("笔记不存在");
            writer.flush();
            return;
        }

        String title = note.getTitle() != null ? note.getTitle() : "无标题";
        String fileName = title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            noteService.exportPdf(id, baos);
        } catch (RuntimeException e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            response.getWriter().flush();
            return;
        }

        response.setContentType("application/pdf");
        setContentDisposition(response, id, fileName);
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", " ");
    }
}
