package top.ccxh.carry.scheduler.task.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.carry.scheduler.service.ActionUserService;
import top.ccxh.carry.scheduler.service.FileInfoService;
import top.ccxh.carry.scheduler.task.TaskAction;
import top.ccxh.common.service.HttpClientService;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

public class BilibiliRecord implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(BilibiliiAction.class);
    private final static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static long MAX_SIZE = (long)((1000L * 1000L*1000L)*1.5D);
   // private final static long MAX_SIZE = (long)((1000L * 1000L*10L));
    //最小上传 为200m
    private final static long MIN_SIZE = (long)((1000L * 1000L*50L));
    private HttpClientService httpClientService;
    private ActionUser actionUser;
    private LinkedBlockingDeque<Object> linkedBlockingDeques;
    private String rootPath;
    private String fileName;
    private ActionUserService actionUserService;
    private String url;
    private FileInfoService fileInfoService;
    //唯一分组id
    private String groupId=null;
    //分p数组
    private List<FileInfo> fileInfoList=null;

    private TaskAction taskAction =null;
    public BilibiliRecord(FileInfoService fileInfoService,ActionUser actionUser,
                          String url, String rootPath, LinkedBlockingDeque<Object> linkedBlockingDeques, ActionUserService actionUserService,HttpClientService httpClientService,TaskAction bilibiliiAction) {
        this.actionUser = actionUser;
        this.url = url;
        this.rootPath = rootPath.concat("/video/");
        this.linkedBlockingDeques = linkedBlockingDeques;
        this.fileInfoService = fileInfoService;
        this.httpClientService=httpClientService;
        this.actionUserService=actionUserService;
        this.groupId=UUID.randomUUID().toString();
        this.fileInfoList=new ArrayList<>();
        this.taskAction =bilibiliiAction;
    }

    private void record(String url, ActionUser user) {
        InputStream content = null;
        CloseableHttpResponse response = httpClientService.doResponse(url);
        if (response == null) {
            LOGGER.info("bid:{}直播已结束,{}", user.getbId(),this.groupId);
            return;
        }
        try {
            content = new BufferedInputStream(response.getEntity().getContent());
        } catch (IOException e) {
            LOGGER.info("{}的直播流获取异常:{},{}", user.getbId(), e.getMessage(),this,groupId);
            return;
        }
        Date startTime = new Date();;
        BufferedOutputStream bufferedOutputStream = null;
        try {
             bufferedOutputStream = getOutput();
            byte buff[] = new byte[4096];
            int len = -1;
            long size = 0;
            while ((len = content.read(buff)) != -1) {
                size += len;
                bufferedOutputStream.write(buff, 0, len);
                if (size >= MAX_SIZE) {
                    //分p
                    size = 0;
                    try {
                        //关闭前强制刷新一次
                        bufferedOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    HttpClientService.closeIO(bufferedOutputStream);
                    HttpClientService.closeIO(response);
                    String actionUrl = taskAction.getActionUrl(this.actionUser.getbId());
                    response = httpClientService.doResponse(actionUrl);
                    if (response == null) {
                        LOGGER.info("bid:{}稿件分p失败或直播已结束,{}", user.getbId(),this.groupId,actionUrl);
                        return;
                    }
                    LOGGER.info("bid:{}-稿件分p,{}", user.getbId(),this.groupId);
                    //保存list
                    addList(startTime);
                    //重置开始时间
                    startTime = new Date();
                    //切换新拍的流
                    content=new BufferedInputStream(response.getEntity().getContent());
                    bufferedOutputStream = getOutput();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭前强制刷新一次
                bufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpClientService.closeIO(bufferedOutputStream);
            /*HttpClientService.closeIO(content);*/
            HttpClientService.closeIO(response);
            addList(startTime);
            finalAddDeques();
        }
    }

    /**
     * 添加到分p数组中
     *
     * @param startTime
     */
    private void addList(Date startTime) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.length() > MIN_SIZE) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilePath(file.getAbsolutePath());
                fileInfo.setEndTime(new Date());
                fileInfo.setStartTime(startTime);
                fileInfo.setUserId(this.actionUser.getId());
                fileInfo.setGroupId(this.groupId);
                this.fileInfoService.insertFileInfo(fileInfo);
                this.fileInfoList.add(fileInfo);
            }else {
                //不符合删除
                file.delete();
            }
        }
    }

    /**
     * 分发
     */
    private void finalAddDeques(){
        JSONObject object = new JSONObject();
        if (this.fileInfoList.size()<=0){
            return;
        }
        object.put("file", this.fileInfoList);
        object.put("user", actionUser);
        LOGGER.info("add deque:{}",JSON.toJSONString(object));
        linkedBlockingDeques.offer(object);
    }

    /**
     * 获取新的流
     *
     * @return
     * @throws FileNotFoundException
     */
    private BufferedOutputStream getOutput() throws FileNotFoundException {
        fileName = rootPath.concat(actionUser.getUserName()).concat("_")
                .concat(LocalDateTime.now().format(yyyyMMdd)).concat("_")
                .concat(actionUser.getbId()).concat("_")
                .concat(Thread.currentThread().getName()).concat("_") + ((int)(Math.random()*10000))+((int)(Math.random()*10000))+".flv";
        return new BufferedOutputStream(new FileOutputStream(fileName));
    }

    @Override
    public void run() {
        actionUserService.actionUserPlay(this.actionUser.getId());
        LOGGER.info("start-Thread Name{}-{}-{},{}",Thread.currentThread().getName(),actionUser.getUserName(),actionUser.getId(),groupId);
        try {
            record(this.url, this.actionUser);
        }catch (Exception e){
           LOGGER.info("异常中断");
        }
        LOGGER.info("end-Thread Name{}-{}-{},{}",Thread.currentThread().getName(),actionUser.getUserName(),actionUser.getId(),groupId);
        actionUserService.actionUserPlayOver(this.actionUser.getId());
    }

}
