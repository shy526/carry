package top.ccxh.carry.scheduler.config;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.ccxh.carry.mapper.anno.ActionUserMapper;
import top.ccxh.carry.mapper.anno.FileInfoMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.carry.scheduler.task.DequeManger;
import top.ccxh.carry.scheduler.upload.BilibliUpLoad;

import java.io.File;
import java.util.List;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class ApplicationRun implements ApplicationRunner {
    private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationRun.class);
    @Autowired
    DequeManger dequeManger;
    @Autowired
    BilibliUpLoad bilibliUpLoad;

    @Autowired
    ActionUserMapper actionUserMapper;

    @Autowired
    FileInfoMapper fileInfoMapper;
    @Value("${file.root}")
    private String fileRoot;
    @Override
    public void run(ApplicationArguments args) throws Exception {

       actionUserInit();
        fileInfoInit();
        environmentInit();

        //开启上传线程(唯一)
        new Thread(()->{
            LOGGER.info("Thread:{}---{}:start",Thread.currentThread().getName(),bilibliUpLoad.getClass().getSimpleName());
            while (true) {
                try {
                    JSONObject take = (JSONObject) dequeManger.getDeque().take();
                    bilibliUpLoad.upload(take);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 将所有正在提交的file修改
     */
    private void fileInfoInit() {
        FileInfo condition = new FileInfo();
        condition.setFlag(3);
        List<FileInfo> select = fileInfoMapper.select(condition);
        fileInfoMapper.updateBathFileInfoByid(2,select);
    }

    /**
     * 将所有正在抓取的修正
     */
    private void actionUserInit() {
        ActionUser condition =new ActionUser();
        condition.setFlag(1);
        List<ActionUser> select = actionUserMapper.select(condition);
        condition.setFlag(0);
        for (ActionUser user:select){
            condition.setId(user.getId());
            actionUserMapper.updateByPrimaryKeySelective(condition);
        }
    }

    /**
     * 初始化环境
     */
    private void environmentInit(){
        String png = fileRoot.concat("/png");
        File file=new File(png);
        if (!file.exists()){
            file.mkdirs();
        }
        String video = fileRoot.concat("/video");
         file=new File(png);
        if (!file.exists()){
            file.mkdirs();
        }

    }
}
