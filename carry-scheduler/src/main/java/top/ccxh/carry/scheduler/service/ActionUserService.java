package top.ccxh.carry.scheduler.service;

import top.ccxh.carry.mapper.pojo.ActionUser;

import java.util.List;

public interface ActionUserService {
    /**
     * 根据 ID 更新记录的flag
     * @param id
     * @param flag
     */
     void updateFlag(int id, int flag);

    /**
     *
     * @param type
     * @param flag
     * @return
     */
     List<ActionUser> selectActionUserByTypeAndFlag(int type, int flag);

    /**
     * 查找没有开播的主播
     * @return
     */
    List<ActionUser>  selectActionUserNoPlay();

    void actionUserPlay(int id);
    void actionUserPlayOver(int id);

    ActionUser selectActionUserById(int id);
}
