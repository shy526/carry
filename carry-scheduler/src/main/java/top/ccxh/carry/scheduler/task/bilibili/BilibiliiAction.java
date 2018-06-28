package top.ccxh.carry.scheduler.task.bilibili;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.carry.scheduler.service.ActionUserService;
import top.ccxh.carry.scheduler.service.FileInfoService;
import top.ccxh.carry.scheduler.task.DequeManger;
import top.ccxh.carry.scheduler.task.TaskAction;
import top.ccxh.common.service.HttpClientService;
import top.ccxh.common.utils.ThreadPoolUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 抓取github的定时任务
 *
 * @author honey
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class BilibiliiAction implements TaskAction {
    private final static Logger log = LoggerFactory.getLogger(BilibiliiAction.class);
    private final static String ROOM_URL = "https://api.live.bilibili.com/room/v1/Room/room_init?id=%s";
    private final static String PAY_URL = "https://api.live.bilibili.com/room/v1/Room/playUrl?cid=%s&quality=0&platform=web";
    ThreadPoolExecutor threadPool = ThreadPoolUtil.getThreadPool();
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ActionUserService actionUserService;
    @Autowired
    private HttpClientService httpClientService;
    @Autowired
    private DequeManger dequeManger;
    @Value("${file.root}")
    private String fileRoot;
    @Autowired
   private FileInfoService fileInfoService;

    @Scheduled(cron = "20/1 * * * * ? ")
    @Override
    public void scan() {
        CloseableHttpResponse response = null;
        List<ActionUser> actionUsers = actionUserService.selectActionUserNoPlay();
        for (ActionUser user : actionUsers) {
            try {
                String actionUrl = getActionUrl(user.getbId());
                response = httpClientService.doResponse(actionUrl);
                if (response == null) {
                    HttpClientService.closeIO(response);
                    continue;
                }
                actionUserService.actionUserPlay(user.getId());
                threadPool.execute(new BilibiliRecord(fileInfoService,user,actionUrl,fileRoot,dequeManger.getDeque(),actionUserService,httpClientService,this));
            } catch (Exception e) {
                //将获取错误的的
                HttpClientService.closeIO(response);
            }
        }
    }


    /**
     * 补交失败的文件
     */
    @Scheduled(cron = "50/1 * * * * ? ")
    @Override
    public void repairUpload(){
        Map<String, List<FileInfo>> groupList = fileInfoService.selectFileInfoGropByflagEq2();

        for (Map.Entry<String, List<FileInfo>> entry :groupList.entrySet()){
            JSONObject object = new JSONObject();
            object.put("file", entry.getValue());
            object.put("user", actionUserService.selectActionUserById(entry.getValue().get(0).getUserId()));
            dequeManger.getDeque().offer(object);
        }

    }



    /**
     * 获取直播流的url
     *
     * @param roomId
     * @return
     * @throws Exception
     */
    @Override
    public String getActionUrl(String roomId) throws Exception {
        String s = httpClientService.doGet(String.format(ROOM_URL, roomId));
        JSONObject jsonObject = JSON.parseObject(s);
        Object room = jsonObject.getJSONObject("data").getString("room_id");
        String s1 = httpClientService.doGet(String.format(PAY_URL, room));
        JSONArray jsonArray = JSON.parseObject(s1).getJSONObject("data").getJSONArray("durl");
        return jsonArray.getJSONObject(0).getString("url");
    }


}
