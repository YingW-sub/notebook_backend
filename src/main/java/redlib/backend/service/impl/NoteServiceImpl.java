package redlib.backend.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import redlib.backend.constant.NoteRecycleConstants;
import redlib.backend.dao.AdminMapper;
import redlib.backend.dao.CategoryMapper;
import redlib.backend.dao.NoteCategoryRelationMapper;
import redlib.backend.dao.NoteMapper;
import redlib.backend.dto.CategoryDTO;
import redlib.backend.dto.NoteDTO;
import redlib.backend.dto.NoteImportResultDTO;
import redlib.backend.dto.ReorderNotesDTO;
import redlib.backend.dto.query.NoteQueryDTO;
import redlib.backend.model.Admin;
import redlib.backend.model.Category;
import redlib.backend.model.Note;
import redlib.backend.model.NoteCategoryRelation;
import redlib.backend.model.Page;
import redlib.backend.model.Token;
import redlib.backend.service.NoteService;
import redlib.backend.utils.PageUtils;
import redlib.backend.utils.ThreadContextHolder;
import redlib.backend.vo.NoteVO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    private static final long NOTE_IMPORT_MAX_BYTES = 15L * 1024 * 1024;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NoteCategoryRelationMapper relationMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AdminMapper adminMapper;

    private Token token() {
        return ThreadContextHolder.getToken();
    }

    private void assertNotRoot() {
        if (token().isRoot()) {
            throw new IllegalStateException("非法操作：管理员无权访问或修改用户私人笔记");
        }
    }

    private boolean isOwner(Note note, long userId) {
        return note.getUserId() != null && note.getUserId().equals(userId);
    }

    private boolean canReadNote(Note note, long userId) {
        return isOwner(note, userId);
    }
    //获取笔记列表
    @Override
    public Page<NoteVO> listByPage(NoteQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new NoteQueryDTO();
        }

        if (token().isRoot()) {
            queryDTO.setIsRoot(true);
        } else {
            Long userId = token().getUserId().longValue();
            queryDTO.setUserId(userId);
            queryDTO.setIsRoot(false);
        }

        if (StringUtils.hasText(queryDTO.getOwnerUserCode())) {
            String t = queryDTO.getOwnerUserCode().trim();
            queryDTO.setOwnerUserCode(t);
            if (t.matches("^\\d+$")) {
                try {
                    queryDTO.setOwnerAdminId(Integer.parseInt(t));
                } catch (NumberFormatException ignored) {
                    queryDTO.setOwnerAdminId(null);
                }
            } else {
                queryDTO.setOwnerAdminId(null);
            }
        }

        Integer size = noteMapper.count(queryDTO);
        PageUtils pageUtils = new PageUtils(queryDTO.getCurrent(), queryDTO.getPageSize(), size);

        if (size == 0) {
            return pageUtils.getNullPage();
        }

        List<Note> list = noteMapper.list(queryDTO, pageUtils.getOffset(), pageUtils.getLimit());

        Map<Long, Admin> ownersByUserId = Collections.emptyMap();
        if (token().isRoot() && !list.isEmpty()) {
            List<Integer> adminIds = list.stream()
                    .map(Note::getUserId)
                    .filter(Objects::nonNull)
                    .map(Long::intValue)
                    .distinct()
                    .collect(Collectors.toList());
            if (!adminIds.isEmpty()) {
                List<Admin> admins = adminMapper.listByIds(adminIds);
                ownersByUserId = new HashMap<>(admins.size() * 2);
                for (Admin a : admins) {
                    ownersByUserId.put(a.getId().longValue(), a);
                }
            }
        }

        Map<Long, Admin> finalOwners = ownersByUserId;
        List<NoteVO> voList = list.stream()
                .map(n -> convertToVO(n, finalOwners))
                .collect(Collectors.toList());

        return new Page<>(pageUtils.getCurrent(), pageUtils.getPageSize(), pageUtils.getTotal(), voList);
    }
    //添加笔记
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addNote(NoteDTO noteDTO) {
        assertNotRoot();  //对root不放行
        Long userId = token().getUserId().longValue();

        Assert.hasText(noteDTO.getTitle(), "笔记标题不能为空");

        Note note = new Note();
        BeanUtils.copyProperties(noteDTO, note);
        note.setUserId(userId);
        note.setIsStarred(false);
        note.setPinned(false);
        Integer maxSo = noteMapper.selectMaxSortOrderByUser(userId);
        note.setSortOrder(maxSo == null || maxSo < 0 ? 0 : maxSo + 1);
        note.setDeleted(false);
        note.setCreateTime(new Date());
        note.setUpdateTime(new Date());

        noteMapper.insertSelective(note);

        if (noteDTO.getCategoryIds() != null && !noteDTO.getCategoryIds().isEmpty()) {
            replaceNoteCategories(note.getId(), noteDTO.getCategoryIds());
        }

        return note.getId();
    }

    @Override
    public NoteDTO getById(Long id) {
        assertNotRoot();//对root不放行
        long userId = token().getUserId().longValue();

        Assert.notNull(id, "笔记ID不能为空");

        Note note = noteMapper.selectByPrimaryKey(id);
        Assert.notNull(note, "笔记不存在");
        Assert.isTrue(canReadNote(note, userId), "无权限查看该笔记");

        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setSummary(note.getSummary());

        List<Long> categoryIds = relationMapper.selectCategoryIdsByNoteId(id);
        dto.setCategoryIds(categoryIds.isEmpty() ? null : categoryIds);

        return dto;
    }
    //更新笔记
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateNote(NoteDTO noteDTO) {
        assertNotRoot();//对root不放行
        long userId = token().getUserId().longValue();

        Assert.notNull(noteDTO.getId(), "笔记ID不能为空");

        Note note = noteMapper.selectByPrimaryKey(noteDTO.getId());
        Assert.notNull(note, "笔记不存在");
        Assert.isTrue(canReadNote(note, userId), "无权限修改该笔记");

        boolean owner = isOwner(note, userId);

        if (owner) {
            if (noteDTO.getTitle() != null) {
                note.setTitle(noteDTO.getTitle());
            }
            if (noteDTO.getContent() != null) {
                note.setContent(noteDTO.getContent());
            }
            if (noteDTO.getSummary() != null) {
                note.setSummary(noteDTO.getSummary());
            }
            if (noteDTO.getCategoryIds() != null) {
                replaceNoteCategories(note.getId(), noteDTO.getCategoryIds());
            }
        } else {
            if (noteDTO.getTitle() != null) {
                note.setTitle(noteDTO.getTitle());
            }
            if (noteDTO.getContent() != null) {
                note.setContent(noteDTO.getContent());
            }
            if (noteDTO.getSummary() != null) {
                note.setSummary(noteDTO.getSummary());
            }
        }

        note.setUpdateTime(new Date());
        noteMapper.updateByPrimaryKeyWithBLOBs(note);

        return note.getId();
    }
    //收藏笔记
    @Override
    public void toggleFavorite(Long id) {
        assertNotRoot();//对root不放行
        Long userId = token().getUserId().longValue();

        Assert.notNull(id, "笔记ID不能为空");

        Note note = noteMapper.selectByPrimaryKey(id);
        Assert.notNull(note, "笔记不存在");
        Assert.isTrue(note.getUserId().equals(userId), "仅笔记所有者可以收藏或取消收藏");

        boolean wasStarred = Boolean.TRUE.equals(note.getIsStarred());
        note.setIsStarred(!wasStarred);
        note.setUpdateTime(new Date());
        noteMapper.updateByPrimaryKeySelective(note);
    }

    //置顶笔记
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void togglePin(Long id) {
        assertNotRoot();
        Long userId = token().getUserId().longValue();

        Assert.notNull(id, "笔记ID不能为空");

        Note note = noteMapper.selectByPrimaryKey(id);
        Assert.notNull(note, "笔记不存在");
        Assert.isTrue(note.getUserId().equals(userId), "无权限操作他人的笔记");
        Assert.isTrue(!Boolean.TRUE.equals(note.getDeleted()), "回收站中的笔记无法置顶");

        note.setPinned(!Boolean.TRUE.equals(note.getPinned()));
        note.setUpdateTime(new Date());
        noteMapper.updateByPrimaryKeySelective(note);
    }
    //重新排序
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderNotes(ReorderNotesDTO body) {
        assertNotRoot();//对root不放行
        Long userId = token().getUserId().longValue();

        Assert.notNull(body, "参数不能为空");
        List<Long> orderedIds = body.getOrderedIds();
        Assert.notEmpty(orderedIds, "排序列表不能为空");

        List<Long> existing = noteMapper.selectIdsByUserNonDeleted(userId);
        Set<Long> eSet = new HashSet<>(existing);
        Set<Long> gSet = new HashSet<>(orderedIds);
        Assert.isTrue(eSet.equals(gSet), "笔记列表与服务器不一致，请刷新页面后重试");

        List<Long> pinnedSeq = new ArrayList<>();
        List<Long> unpinnedSeq = new ArrayList<>();
        for (Long nid : orderedIds) {
            Note n = noteMapper.selectByPrimaryKey(nid);
            Assert.notNull(n, "笔记不存在");
            if (Boolean.TRUE.equals(n.getPinned())) {
                pinnedSeq.add(nid);
            } else {
                unpinnedSeq.add(nid);
            }
        }
        List<Long> canonical = new ArrayList<>(pinnedSeq);
        canonical.addAll(unpinnedSeq);

        int i = 0;
        Date now = new Date();
        for (Long nid : canonical) {
            Note u = new Note();
            u.setId(nid);
            u.setSortOrder(i++);
            u.setUpdateTime(now);
            noteMapper.updateByPrimaryKeySelective(u);
        }
    }
    //导入文档
    @Override
    public NoteImportResultDTO importDocument(MultipartFile file) {
        assertNotRoot();//对root不放行
        Assert.notNull(file, "请选择文件");
        Assert.isTrue(!file.isEmpty(), "文件为空");
        Assert.isTrue(file.getSize() <= NOTE_IMPORT_MAX_BYTES, "文件过大，请上传不超过 15MB 的文档");

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }

        String plain;
        try (InputStream in = file.getInputStream()) {
            plain = switch (ext) {
                case "txt", "text" -> readTxt(in);
                case "docx" -> readDocx(in);
                case "doc" -> readDoc(in);
                case "pdf" -> readPdf(in);
                default -> throw new IllegalArgumentException("不支持的格式，请上传 .txt、.doc、.docx 或 .pdf");
            };
        } catch (IOException e) {
            throw new IllegalStateException("读取文件失败：" + e.getMessage(), e);
        }

        plain = plain == null ? "" : plain.trim();
        Assert.hasText(plain, "未能从文件中提取到可读文本（扫描版 PDF 等无法识别，请换可复制文本的文件）");

        NoteImportResultDTO dto = new NoteImportResultDTO();
        dto.setTitle(suggestImportTitle(original, plain));
        dto.setPlainText(plain);
        dto.setSourceType(ext);
        return dto;
    }
    //生成导入标题
    private static String suggestImportTitle(String filename, String plain) {
        if (StringUtils.hasText(filename)) {
            String base = filename.trim();
            int dot = base.lastIndexOf('.');
            if (dot > 0) {
                base = base.substring(0, dot).trim();
            }
            if (StringUtils.hasText(base)) {
                return base.length() > 200 ? base.substring(0, 200) : base;
            }
        }
        String first = plain.lines().filter(StringUtils::hasText).findFirst().orElse("导入的文档");
        return first.length() > 200 ? first.substring(0, 200) : first;
    }
    //读取Txt文档
    private static String readTxt(InputStream in) throws IOException {
        byte[] bytes = in.readAllBytes();
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
    //读取Docx文档 使用Apache POI
    private static String readDocx(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(in)) {
            for (IBodyElement el : doc.getBodyElements()) {
                if (el instanceof XWPFParagraph p) {
                    String t = p.getText();
                    if (StringUtils.hasText(t)) {
                        sb.append(t).append('\n');
                    }
                } else if (el instanceof XWPFTable tbl) {
                    for (XWPFTableRow row : tbl.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            sb.append(cell.getText()).append('\t');
                        }
                        sb.append('\n');
                    }
                }
            }
        }
        return sb.toString();
    }
    //读取Doc文档 使用Apache POI
    private static String readDoc(InputStream in) throws IOException {
        try (HWPFDocument doc = new HWPFDocument(in)) {
            WordExtractor ex = new WordExtractor(doc);
            return ex.getText();
        }
    }
    //读取PDF文档 使用iText
    private static String readPdf(InputStream in) throws IOException {
        byte[] pdfBytes = in.readAllBytes();
        PdfReader reader = new PdfReader(pdfBytes);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            int n = reader.getNumberOfPages();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= n; i++) {
                sb.append(extractor.getTextFromPage(i));
                if (i < n) {
                    sb.append('\n');
                }
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }
    //删除笔记
    @Override
    public void deleteNote(Long id) {
        assertNotRoot();//对root不放行
        Long userId = token().getUserId().longValue();

        Assert.notNull(id, "笔记ID不能为空");

        Note note = noteMapper.selectByPrimaryKey(id);
        Assert.notNull(note, "笔记不存在");
        Assert.isTrue(note.getUserId().equals(userId), "无权限删除他人的笔记");

        note.setDeleted(true);
        note.setDeleteTime(new Date());
        note.setUpdateTime(new Date());
        noteMapper.updateByPrimaryKeySelective(note);
    }
    //获取回收站笔记列表
    @Override
    public Page<NoteVO> listDeletedNotes(NoteQueryDTO queryDTO) {
        assertNotRoot();//对root不放行
        Long userId = token().getUserId().longValue();

        if (queryDTO == null) {
            queryDTO = new NoteQueryDTO();
        }
        queryDTO.setUserId(userId);
        queryDTO.setIsDeleted(true);

        Integer size = noteMapper.count(queryDTO);
        PageUtils pageUtils = new PageUtils(queryDTO.getCurrent(), queryDTO.getPageSize(), size);

        if (size == 0) {
            return pageUtils.getNullPage();
        }

        List<Note> list = noteMapper.list(queryDTO, pageUtils.getOffset(), pageUtils.getLimit());

        List<NoteVO> voList = list.stream()
                .map(n -> convertToVO(n, Collections.emptyMap()))
                .collect(Collectors.toList());

        return new Page<>(pageUtils.getCurrent(), pageUtils.getPageSize(), pageUtils.getTotal(), voList);
    }
    //恢复笔记
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreNote(Long id) {
        assertNotRoot();//对root不放行
        Long userId = token().getUserId().longValue();

        Assert.notNull(id, "笔记ID不能为空");

        Note note = noteMapper.selectByPrimaryKey(id);
        Assert.notNull(note, "笔记不存在");
        Assert.isTrue(note.getUserId().equals(userId), "无权限操作他人的笔记");
        Assert.isTrue(note.getDeleted(), "该笔记不在回收站中");

        Date deleteTime = note.getDeleteTime();
        Assert.notNull(deleteTime, "删除时间异常，无法恢复");
        long deadline = deleteTime.getTime() + NoteRecycleConstants.RETENTION_DAYS * 24L * 60 * 60 * 1000;
        Assert.isTrue(System.currentTimeMillis() <= deadline,
                "已超过 " + NoteRecycleConstants.RETENTION_DAYS + " 天保留期，该笔记已无法恢复");

        note.setDeleted(false);
        note.setDeleteTime(null);
        note.setUpdateTime(new Date());
        noteMapper.updateByPrimaryKeySelective(note);
    }
    //永久删除笔记
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentDeleteNote(Long id) {
        assertNotRoot();//对root不放行              
        Long userId = token().getUserId().longValue();

        Assert.notNull(id, "笔记ID不能为空");

        Note note = noteMapper.selectByPrimaryKey(id);
        Assert.notNull(note, "笔记不存在");
        Assert.isTrue(note.getUserId().equals(userId), "无权限操作他人的笔记");
        Assert.isTrue(note.getDeleted(), "该笔记不在回收站中");

        relationMapper.deleteByNoteId(id);
        noteMapper.deleteByPrimaryKey(id);
    }
    //获取分类统计
    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        Long userId = token().getUserId().longValue();
        return noteMapper.countByCategory(userId);
    }
    
    private NoteVO convertToVO(Note note, Map<Long, Admin> ownersByUserId) {
        NoteVO vo = new NoteVO();
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        // root 用户列表中不显示摘要，避免泄露用户笔记内容概要
        if (!token().isRoot()) {
            vo.setSummary(note.getSummary());
        }
        vo.setIsStarred(note.getIsStarred());
        vo.setPinned(note.getPinned());
        vo.setDeleted(note.getDeleted());
        vo.setDeleteTime(note.getDeleteTime());
        vo.setCreateTime(note.getCreateTime());
        vo.setUpdateTime(note.getUpdateTime());

        if (note.getUserId() != null && ownersByUserId != null && !ownersByUserId.isEmpty()) {
            vo.setOwnerUserId(note.getUserId());
            Admin owner = ownersByUserId.get(note.getUserId());
            if (owner != null) {
                vo.setOwnerUserCode(owner.getUserCode());
                vo.setOwnerName(owner.getName());
            } else {
                vo.setOwnerUserCode("未知用户(id=" + note.getUserId() + ')');
            }
        }

        List<Long> categoryIds = relationMapper.selectCategoryIdsByNoteId(note.getId());
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> activeCategories = categoryMapper.selectActiveByIds(categoryIds);
            List<CategoryDTO> categories = activeCategories.stream().map(cat -> {
                CategoryDTO dto = new CategoryDTO();
                dto.setId(cat.getId());
                dto.setCategoryName(cat.getCategoryName());
                return dto;
            }).collect(Collectors.toList());
            vo.setCategories(categories);
        } else {
            vo.setCategories(Collections.emptyList());
        }

        return vo;
    }

    /**
     * 先清空该笔记的旧关联，再写入
     */
    private void replaceNoteCategories(Long noteId, List<Long> categoryIds) {
        relationMapper.deleteByNoteId(noteId);
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        List<Long> distinct =
                categoryIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return;
        }
        Date now = new Date();
        List<NoteCategoryRelation> relations = distinct.stream().map(categoryId -> {
            NoteCategoryRelation relation = new NoteCategoryRelation();
            relation.setNoteId(noteId);
            relation.setCategoryId(categoryId);
            relation.setCreateTime(now);
            return relation;
        }).collect(Collectors.toList());
        relationMapper.batchInsert(relations);
    }

    private static String htmlToPlainText(String html) {
        if (html == null) {
            return "";
        }
        String t = html.replaceAll("(?s)<script.*?>.*?</script>", " ");
        t = t.replaceAll("<[^>]+>", " ");
        t = t.replace("&nbsp;", " ").replaceAll("\\s+", " ").trim();
        return t;
    }

    private static final Pattern ANNOTATION_PATTERN =
            Pattern.compile("【批注】\\s*([\\s\\S]*?)(?=【批注】|$)");

    /** 与前端 extractNoteAnnotations 一致：从正文中解析批注正文列表 */
    private static List<String> extractAnnotationBodies(String html) {
        if (html == null || html.isBlank()) {
            return Collections.emptyList();
        }
        String plain = htmlToPlainText(html);
        List<String> out = new ArrayList<>();
        Matcher m = ANNOTATION_PATTERN.matcher(plain);
        while (m.find()) {
            String s = m.group(1).trim().replaceAll("\\s+", " ");
            if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }
//导出Word文档
    @Override
    public void exportWord(Long id, OutputStream os) {
        NoteDTO note = this.getById(id);
        // 标题：居中、18号、加粗
        try (XWPFDocument document = new XWPFDocument()) {
            if (note.getTitle() != null && !note.getTitle().isBlank()) {
                XWPFParagraph titlePara = document.createParagraph();
                titlePara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setText(note.getTitle());
                titleRun.setBold(true);
                titleRun.setFontSize(18);
            }
            // 摘要：斜体、12号
            if (note.getSummary() != null && !note.getSummary().isBlank()) {
                XWPFParagraph summaryPara = document.createParagraph();
                XWPFRun summaryRun = summaryPara.createRun();
                summaryRun.setText(note.getSummary());
                summaryRun.setItalic(true);
                summaryRun.setFontSize(12);
                document.createParagraph();
            }
            
            if (note.getContent() != null && !note.getContent().isBlank()) {
                // 正文：先 strip HTML，再写入
                XWPFParagraph contentPara = document.createParagraph();
                XWPFRun contentRun = contentPara.createRun();
                contentRun.setText(htmlToPlainText(note.getContent()));
                contentRun.setFontSize(12);
            }

            List<String> annotations = extractAnnotationBodies(note.getContent());
            if (!annotations.isEmpty()) {
                document.createParagraph();
                // 批注附录
                XWPFParagraph annTitlePara = document.createParagraph();
                XWPFRun annTitleRun = annTitlePara.createRun();
                annTitleRun.setText("附录：批注列表");
                annTitleRun.setBold(true);
                annTitleRun.setFontSize(14);
                XWPFParagraph hintPara = document.createParagraph();
                XWPFRun hintRun = hintPara.createRun();
                hintRun.setText("（以下为文中「【批注】」汇总；Word 原生修订侧栏批注需用 Word 打开后再添加。）");
                hintRun.setItalic(true);
                hintRun.setFontSize(10);
                int n = 1;
                for (String body : annotations) {
                    XWPFParagraph p = document.createParagraph();
                    p.setIndentationLeft(400);
                    XWPFRun r = p.createRun();
                    r.setText(n + ". " + body);
                    r.setFontSize(11);
                    n++;
                }
            }

            document.write(os);
        } catch (Exception e) {
            throw new RuntimeException("导出 Word 文档失败: " + e.getMessage(), e);
        }
    }
//导出PDF文档
    private BaseFont resolveChineseBaseFont() throws IOException, DocumentException {
        String[][] candidates = {
                {"C:/Windows/Fonts/msyh.ttc,0"},
                {"C:/Windows/Fonts/msyhbd.ttc,0"},
                {"C:/Windows/Fonts/simsun.ttc,0"},
                {"C:/Windows/Fonts/simhei.ttf"},
                {"C:/Windows/Fonts/msyh.ttf"},
        };
        for (String[] c : candidates) {
            String spec = c[0];
            String filePart = spec.contains(",") ? spec.substring(0, spec.indexOf(',')) : spec;
            if (!new File(filePart).isFile()) {
                continue;
            }
            try {
                return BaseFont.createFont(spec, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {
            }
        }
        try {
            return BaseFont.createFont("STSong-Light", "UniGB-UTF16-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        }
    }

    @Override
    public void exportPdf(Long id, OutputStream os) {
        NoteDTO note = this.getById(id);

        try {
            BaseFont bfChinese = resolveChineseBaseFont();
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font contentFont = new Font(bfChinese, 12, Font.NORMAL);

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, os);
            writer.setCloseStream(false);
            document.open();

            if (note.getTitle() != null && !note.getTitle().isBlank()) {
                // 标题：居中、18号、加粗
                Paragraph titlePara = new Paragraph(note.getTitle(), titleFont);
                titlePara.setAlignment(Paragraph.ALIGN_CENTER);
                titlePara.setSpacingAfter(10f);
                document.add(titlePara);
            }

            if (note.getSummary() != null && !note.getSummary().isBlank()) {
                Paragraph summaryPara = new Paragraph("摘要: " + note.getSummary(), contentFont);
                summaryPara.setSpacingAfter(10f);
                document.add(summaryPara);
            }

            if (note.getContent() != null && !note.getContent().isBlank()) {
                String plain = htmlToPlainText(note.getContent());
                for (String line : plain.split("\n")) {
                    document.add(new Paragraph(line.isBlank() ? " " : line, contentFont));
                }
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("导出 PDF 文档失败: " + e.getMessage(), e);
        }
    }
}
