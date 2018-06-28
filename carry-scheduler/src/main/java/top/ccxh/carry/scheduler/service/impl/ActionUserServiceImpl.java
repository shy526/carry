package top.ccxh.carry.scheduler.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ccxh.carry.mapper.anno.ActionUserMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.scheduler.service.ActionUserService;

import java.util.Date;
import java.util.List;

@Service
public class ActionUserServiceImpl implements ActionUserService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ActionUserMapper actionUserMapper;
    private static final int PLAY=1;
    private static final int PLAY_OVER=0;
    @Override
    public void updateFlag(int id, int flag) {
        ActionUser ac = new ActionUser();
        ac.setFlag(flag);
        ac.setId(id);
        if (1 == flag) {
            // 开播
            ac.setActionTime(new Date());
        }
        actionUserMapper.updateByPrimaryKeySelective(ac);
    }

    @Override
    public List<ActionUser> selectActionUserByTypeAndFlag(int type, int flag) {
        ActionUser actionUser = new ActionUser();
        actionUser.setFlag(flag);
        actionUser.setLinkType(type);
        return actionUserMapper.select(actionUser);
    }

    @Override
    public List<ActionUser>  selectActionUserNoPlay() {
        return selectActionUserByTypeAndFlag(0,0);
    }

    @Override
    public void actionUserPlay(int id) {
        this.updateFlag(id, this.PLAY);
    }

    @Override
    public void actionUserPlayOver(int id) {
        this.updateFlag(id, this.PLAY_OVER);
    }

    @Override
    public ActionUser selectActionUserById(int id) {
        ActionUser userCondition = new ActionUser();
        userCondition.setId(id);
        return actionUserMapper.selectByPrimaryKey(userCondition);
    }
}
