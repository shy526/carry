package top.ccxh.carry.scheduler.task.youtube;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.carry.mapper.anno.ActionUserMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.common.service.HttpClientService;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingDeque;

public class YouTubeRecord implements Runnable {
    private final static Logger LOGGER=LoggerFactory.getLogger(YouTubeAction.class);
    private String url;
    private HttpClientService httpClientService;
    private LinkedBlockingDeque<String> down = new LinkedBlockingDeque<>();
    private ActionUser user;
    private ActionUserMapper actionUserMapper;
    private long index = 0L;
    private String fileName;
    private String rootPath;
    private final static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public YouTubeRecord(String url, HttpClientService httpClientService, ActionUser user, ActionUserMapper actionUserMapper,String rootPath) {
        this.url = url;
        this.httpClientService = httpClientService;
        this.user = user;
        this.actionUserMapper = actionUserMapper;
        this.rootPath = rootPath.concat("/video/");
    }

    private boolean record(ActionUser user, String url) throws FileNotFoundException {
        fileName = rootPath.concat(user.getUserName()).concat("_").concat(LocalDateTime.now().format(yyyyMMdd)).concat("_")
                .concat(user.getbId()).concat("_")
                .concat(Thread.currentThread().getName()).concat("_") + System.currentTimeMillis()+".ts";
        BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(new FileOutputStream(fileName));
        new Thread(new Runnable() {
            BufferedInputStream bufferedInputStream=null;
            @Override
            public void run() {
                CloseableHttpResponse response =null;
                byte[] buuf=new byte[1024];
                while (true){
                    try {
                        String take = down.take();
                        if (take.equals("1")){
                            return;
                        }
                         response = httpClientService.doResponse(take);
                        if (response==null){
                            LOGGER.info("response is null:{}",user.getbId());
                            continue;
                        }
                        bufferedInputStream = new BufferedInputStream(response.getEntity().getContent());
                        int len=0;
                        while ((len=bufferedInputStream.read(buuf))!=-1){
                            bufferedOutputStream.write(buuf,0,len);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        HttpClientService.closeIO(bufferedInputStream);
                        HttpClientService.closeIO(response);
                    }

                }
            }
        }).start();

        while (true) {
            String s = httpClientService.doGet("127.0.0.1", 1021, url);
            if (null == s && "".equals(s)) {
                String[] split = s.split("\\n");
                if (split.length >= 7) {
                    for (int i = 7; i < split.length; i++) {
                        if (i % 2 != 0) {
                            down.offer(split[i]);
                        }
                    }
                }
            } else {
                break;
            }
            try {
                Thread.sleep(2*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        down.offer("-1");
        return true;
    }

    @Override
    public void run() {
        updateflage(1);
        try {
            record(user, url);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        updateflage(0);
    }

    private void updateflage(int i) {
        ActionUser condition = new ActionUser();
        condition.setFlag(1);
        condition.setId(user.getId());
        actionUserMapper.updateByPrimaryKeySelective(condition);
    }
}
