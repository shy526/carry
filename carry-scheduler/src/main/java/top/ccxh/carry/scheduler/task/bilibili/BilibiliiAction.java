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
import top.ccxh.carry.mapper.anno.ActionUserMapper;
import top.ccxh.carry.mapper.anno.FileInfoMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.carry.scheduler.task.DequeManger;
import top.ccxh.carry.scheduler.upload.BilibliUpLoad;
import top.ccxh.common.service.HttpClientService;
import top.ccxh.common.utils.ThreadPoolUtil;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 抓取github的定时任务
 *
 * @author honey
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
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
                    HttpClientService.closeIO(response);
                    continue;
                }
                updateFla(user,1);
                threadPool.execute(new BilibiliRecord(fileInfoMapper,user,actionUrl,fileRoot,dequeManger.getDeque(),actionUserMapper,httpClientService));
            } catch (Exception e) {
                //将获取错误的的
                HttpClientService.closeIO(response);
            }
        }
    }

    /**
     * 脏读
     * @param user
     * @param i
     */
    private void updateFla(ActionUser user,int i) {
        ActionUser ac = new ActionUser();
        ac.setFlag(i);
        ac.setId(user.getId());
        if (1 == i) {
            ac.setActionTime(new Date());
        }
        actionUserMapper.updateByPrimaryKeySelective(ac);
    }

    /**
     * 补交失败的文件
     */
    private Map<String,List<FileInfo>> groupList=new HashMap<String,List<FileInfo>>();
    @Scheduled(cron = "20/1 * * * * ? ")
    public void payUPload(){
        groupList.clear();
        FileInfo condition =new FileInfo();
        //test 5 2
        condition.setFlag(2); //补交也是以分p方式补交
        List<FileInfo> select = fileInfoMapper.select(condition);
        Map<String, List<FileInfo>> stringListMap = groupFileinfo(select);
        ActionUser userCondition = new ActionUser();
        for (Map.Entry<String, List<FileInfo>> entry :this.groupList.entrySet()){
            JSONObject object = new JSONObject();
            object.put("file", entry.getValue());
            userCondition.setId(entry.getValue().get(0).getUserId());
            object.put("user", actionUserMapper.selectByPrimaryKey(userCondition));
            dequeManger.getDeque().offer(object);
        }

    }

    /**
     * 对结果集进行分组,并修改标记
     * @param select
     * @return
     */
    private Map<String, List<FileInfo>> groupFileinfo(List<FileInfo> select) {
        List<JSONObject> objects=new ArrayList<>();
        for (FileInfo fileInfo:select){
            List<FileInfo> fileInfos = groupList.get(fileInfo.getGroupId());
            if (fileInfos==null){
                fileInfos= new ArrayList<>();
            }
            fileInfos.add(fileInfo);
            this.groupList.put(fileInfo.getGroupId(),fileInfos);
        }
        if (select.size()>0){
            this.fileInfoMapper.updateBathFileInfoByid(3,select);
        }

        return groupList;
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
