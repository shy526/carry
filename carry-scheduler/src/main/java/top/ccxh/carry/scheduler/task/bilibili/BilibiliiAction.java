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
import top.ccxh.carry.mapper.anno.ActionUserMapper;
import top.ccxh.carry.mapper.anno.FileInfoMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.carry.scheduler.task.DequeManger;
import top.ccxh.carry.scheduler.upload.BilibliUpLoad;
import top.ccxh.common.service.HttpClientService;
import top.ccxh.common.utils.ThreadPoolUtil;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 抓取github的定时任务
 *
 * @author honey
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
/*@Component*/
public class BilibiliiAction {
    private final static Logger log = LoggerFactory.getLogger(BilibiliiAction.class);
    private final static String ROOM_URL = "https://api.live.bilibili.com/room/v1/Room/room_init?id=%s";
    private final static String PAY_URL = "https://api.live.bilibili.com/room/v1/Room/playUrl?cid=%s&quality=0&platform=web";
    ThreadPoolExecutor threadPool = ThreadPoolUtil.getThreadPool();
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ActionUserMapper actionUserMapper;
    @Autowired
    private HttpClientService httpClientService;
    @Autowired
    private DequeManger dequeManger;
    @Value("${file.root}")
    private String fileRoot;
    @Autowired
    FileInfoMapper fileInfoMapper;

    @Autowired
    private BilibliUpLoad bilibliUpLoad;
    @Scheduled(cron = "10/1 * * * * ? ")
    public void scan() {
        CloseableHttpResponse response = null;
        ActionUser actionUser = new ActionUser();
        actionUser.setFlag(0);
        actionUser.setLinkType(0);
        List<ActionUser> select = actionUserMapper.select(actionUser);
        for (ActionUser user : select) {
            try {
                String actionUrl = getActionUrl(user.getbId());
                response = httpClientService.doResponse(actionUrl);
                if (response == null) {
                    log.info("bid:{}没有开始直播", user.getbId());
                    HttpClientService.closeIO(response);
                    continue;
                }
                threadPool.execute(new BilibiliRecord(fileInfoMapper,user,actionUrl,fileRoot,dequeManger.getDeque(),actionUserMapper,httpClientService));
            } catch (Exception e) {
                //将获取错误的的
                HttpClientService.closeIO(response);
            }
        }
    }


    /**
     * 补交失败的文件
     */
    @Scheduled(cron = "20/1 * * * * ? ")
    public void payUPload(){
        FileInfo condition =new FileInfo();
        condition.setFlag(2);
        List<FileInfo> select = fileInfoMapper.select(condition);
        condition.setFlag(3);
        ActionUser userCondition = new ActionUser();
        for (FileInfo fileInfo:select){
            condition.setId(fileInfo.getId());
            fileInfoMapper.updateByPrimaryKeySelective(condition);
            JSONObject object = new JSONObject();
            object.put("file", fileInfo);
            userCondition.setId(fileInfo.getUserId());
            object.put("user", actionUserMapper.selectByPrimaryKey(userCondition));
            bilibliUpLoad.upload(object);
        };
    }

    /**
     * 获取直播流的url
     *
     * @param roomId
     * @return
     * @throws Exception
     */
    private String getActionUrl(String roomId) throws Exception {
        String s = httpClientService.doGet(String.format(ROOM_URL, roomId));
        JSONObject jsonObject = JSON.parseObject(s);
        Object room = jsonObject.getJSONObject("data").getString("room_id");
        String s1 = httpClientService.doGet(String.format(PAY_URL, room));
        JSONArray jsonArray = JSON.parseObject(s1).getJSONObject("data").getJSONArray("durl");
        return jsonArray.getJSONObject(0).getString("url");
    }


}
