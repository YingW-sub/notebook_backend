package redlib.backend.service;

import redlib.backend.dto.AdminDTO;
import redlib.backend.dto.query.KeywordQueryDTO;
import redlib.backend.model.Page;
import redlib.backend.vo.AdminVO;
import redlib.backend.vo.ModuleVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AdminService {
    /**
     * 获取所有的模块列表
     *
     * @return 所有的模块列表
     */
    List<ModuleVO> listModules();

    Map<Integer, String> getNameMap(Set<Integer> adminIds);

    Page<AdminVO> list(KeywordQueryDTO queryDTO);

    AdminDTO getDetail(Integer id);

    Integer update(AdminDTO adminDTO);

    Integer add(AdminDTO adminDTO);

    Integer delete(List<Integer> ids);

    void updatePassword(String oldPassword, String password);

    /**
     * 获取各用户笔记数量统计（管理员系统活跃度 Top N）
     */
    List<Map<String, Object>> getUserNoteCountStats(int topN);

    /**
     * 全站回收站笔记数量
     */
    Integer getGlobalDeletedNoteCount();

    /**
     * 管理员数据统计：全站有效笔记数、非 root 用户数
     */
    Map<String, Integer> getStatsOverview();
}
