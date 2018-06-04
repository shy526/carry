package top.ccxh.carry.scheduler.task;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.carry.mapper.anno.ActionUserMapper;
import top.ccxh.carry.mapper.anno.FileInfoMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.common.service.HttpClientService;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

public class BilibiliRecord implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(BilibiliiAction.class);
    private final static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static long MAX_SIZE = (long)(1000L * 1000L*1000L*3.7D);
    private HttpClientService httpClientService;
    private ActionUser actionUser;
    private LinkedBlockingDeque<Object> linkedBlockingDeques;
    private String rootPath;
    private String fileName;
    private ActionUserMapper actionUserMapper;
    private long index = 0;
    private String url;
   private FileInfoMapper fileInfoMapper;
    public BilibiliRecord(FileInfoMapper fileInfoMapper,ActionUser actionUser, String url, String rootPath, LinkedBlockingDeque<Object> linkedBlockingDeques, ActionUserMapper actionUserMapper,HttpClientService httpClientService) {
        this.actionUser = actionUser;
        this.url = url;
        this.rootPath = rootPath.concat("/video/");
        this.linkedBlockingDeques = linkedBlockingDeques;
        this.actionUserMapper = actionUserMapper;
        this.httpClientService=httpClientService;
        this.fileInfoMapper=fileInfoMapper;
    }

    private void record(String url, ActionUser user) {
        InputStream content = null;
        CloseableHttpResponse response = httpClientService.doResponse(url);
        if (response == null) {
            log.info("bid:{}直播已结束", user.getbId());
            return;
        }
        try {
            content = new BufferedInputStream(response.getEntity().getContent());
        } catch (IOException e) {
            log.info("{}的直播流获取异常:{}", user.getbId(), e.getMessage());
            return;
        }
        Date startTime = new Date();;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            fileName = rootPath.concat(actionUser.getUserName()).concat("_").concat(LocalDateTime.now().format(yyyyMMdd)).concat("_").concat(actionUser.getbId()).concat("_").concat(Thread.currentThread().getName()).concat("_") + System.currentTimeMillis();
            bufferedOutputStream = getOutput();
            byte buff[] = new byte[2048];
            int len = -1;
            long size = 0;
            while ((len = content.read(buff)) != -1) {
                size += len;
                bufferedOutputStream.write(buff, 0, len);
                if (size >= MAX_SIZE) {
                    size = 0;
                    try {
                        //关闭前强制刷新一次
                        bufferedOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    HttpClientService.closeIO(bufferedOutputStream);
                    HttpClientService.closeIO(response);
                    HttpClientService.closeIO(content);
                    //先分发
                    dispense(startTime);
                    //重置开始时间
                    startTime = new Date();
                    response = httpClientService.doResponse(url);
                    if (response == null) {
                        log.info("bid:{}直播已结束", user.getbId());
                        return;
                    }
                    content=new BufferedInputStream(response.getEntity().getContent());
                    bufferedOutputStream = getOutput();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭前强制刷新一次
                bufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpClientService.closeIO(bufferedOutputStream);
            HttpClientService.closeIO(content);
            HttpClientService.closeIO(response);
            dispense(startTime);
        }
    }

    /**
     * 分发到队列中
     *
     * @param startTime
     */
    private void dispense(Date startTime) {
        File file = new File(fileName + index + ".flv");
        if (file.exists()) {
            if (file.length() > 1) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilePath(file.getAbsolutePath());
                fileInfo.setEndTime(new Date());
                fileInfo.setStartTime(startTime);
                fileInfo.setCreateTime(new Date());
                fileInfo.setUpdateTime(fileInfo.getCreateTime());
                fileInfo.setUserId(actionUser.getId());
                fileInfoMapper.insertSelective(fileInfo);
                JSONObject object = new JSONObject();
                object.put("file", fileInfo);
                object.put("user", actionUser);
                linkedBlockingDeques.offer(object);
            }
        }
    }

    /**
     * 获取新的流并 idnex+1
     *
     * @return
     * @throws FileNotFoundException
     */
    private BufferedOutputStream getOutput() throws FileNotFoundException {

        index++;
        return new BufferedOutputStream(new FileOutputStream(fileName + index + ".flv"));
    }

    @Override
    public void run() {
        updateFla(1);
        record(this.url, this.actionUser);
        updateFla(0);
    }

    /**
     * 更新用户标志位,
     * =1 时会添加闲的actionTime
     * @param i
     */
    private void updateFla(int i) {
        ActionUser ac = new ActionUser();
        ac.setFlag(i);
        ac.setId(actionUser.getId());
        if (1 == i) {
            ac.setActionTime(new Date());
        }
        actionUserMapper.updateByPrimaryKeySelective(ac);
    }
}
