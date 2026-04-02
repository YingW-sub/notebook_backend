package redlib.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redlib.backend.annotation.BackendModule;
import redlib.backend.annotation.Privilege;
import redlib.backend.dto.AdminDTO;
import redlib.backend.dto.query.KeywordQueryDTO;
import redlib.backend.model.Page;
import redlib.backend.service.AdminService;
import redlib.backend.vo.AdminVO;
import redlib.backend.vo.ModuleVO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@BackendModule({"page:页面", "update:修改", "add:创建", "delete:删除"})
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("listModules")
    @Privilege("page")
    public List<ModuleVO> listModules() {

        return adminService.listModules();
    }

    @PostMapping("list")
    @Privilege("page")
    public Page<AdminVO> listAdmin(@RequestBody KeywordQueryDTO queryDTO) {
        return adminService.list(queryDTO);
    }

    @GetMapping("get")
    @Privilege("page")
    public AdminDTO getAdmin(Integer id) {

        return adminService.getDetail(id);
    }

    @PostMapping("add")
    @Privilege("add")
    public Integer addAdmin(@RequestBody AdminDTO adminDTO) {

        return adminService.add(adminDTO);
    }

    @PostMapping("update")
    @Privilege("update")
    public Integer updateAdmin(@RequestBody AdminDTO adminDTO) {

        return adminService.update(adminDTO);
    }

    @PostMapping("delete")
    @Privilege("delete")
    public Integer deleteAdmin(@RequestBody List<Integer> ids) {
        return adminService.delete(ids);
    }

    /**
     * 各用户笔记数量排名 Top N（管理员系统活跃度统计），仅 root / 管理员可访问。
     */
    @GetMapping("stats/userNoteCount")
    @Privilege("page")
    public List<Map<String, Object>> getUserNoteCountStats(
            @RequestParam(value = "topN", defaultValue = "10") int topN) {
        return adminService.getUserNoteCountStats(topN);
    }

    /**
     * 全站回收站笔记数量（管理员数据统计页）
     */
    @GetMapping("stats/deletedNoteCount")
    @Privilege("page")
    public Integer getGlobalDeletedNoteCount() {
        return adminService.getGlobalDeletedNoteCount();
    }

    /**
     * 数据统计页：全站有效笔记总数、系统用户数（不含 root）
     */
    @GetMapping("stats/overview")
    @Privilege("page")
    public Map<String, Integer> getStatsOverview() {
        return adminService.getStatsOverview();
    }
}
