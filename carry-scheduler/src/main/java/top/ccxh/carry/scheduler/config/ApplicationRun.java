package top.ccxh.carry.scheduler.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.ccxh.carry.scheduler.task.DequeManger;
import top.ccxh.carry.scheduler.upload.BilibliUpLoad;

@Component
public class ApplicationRun implements ApplicationRunner {
    @Autowired
    DequeManger dequeManger;
    @Autowired
    BilibliUpLoad bilibliUpLoad;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(()->{
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
}
