package top.ccxh.carry.scheduler.task.youtube;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeRecord implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(YouTubeAction.class);
    private String url;
    private HttpClientService httpClientService;
    private LinkedBlockingDeque<String> down = new LinkedBlockingDeque<>();
    private ActionUser user;
    private ActionUserMapper actionUserMapper;
    private Long index = null;
    private String fileName;
    private String rootPath;
    private final static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static Pattern pattern = Pattern.compile("https:.*/sq/(\\w+)");
    private final static long MAX_SIZE = 1000L * 1000L * 1000L * 2L;
    private FileInfoMapper fileInfoMapper;
    private LinkedBlockingDeque<Object> linkedBlockingDeques;
    public YouTubeRecord(String url, HttpClientService httpClientService, ActionUser user, ActionUserMapper actionUserMapper, String rootPath,FileInfoMapper fileInfoMapper,LinkedBlockingDeque<Object> linkedBlockingDeques) {
        this.url = url;
        this.httpClientService = httpClientService;
        this.user = user;
        this.actionUserMapper = actionUserMapper;
        this.rootPath = rootPath.concat("/video/");
        this.fileInfoMapper=fileInfoMapper;
        this.linkedBlockingDeques=linkedBlockingDeques;
    }

    private boolean record(ActionUser user, String url) throws FileNotFoundException {
        downloadStart();

        while (true) {
            long squence=0;
            String s = httpClientService.doGet("127.0.0.1", 1021, url);
            if (null != s && !"".equals(s)) {
                String[] split = s.split("\\n");
                if (index == null) {
                    index=Long.parseLong(split[3].split(":")[1]);
                    //提示开流
                    down.offer("1");
                    //拿取第一个流序号
                } else {
//                   if (index>Long.parseLong(split[3].split(":")[1])){
//                       continue;
//                   }
                }
                if (split.length >= 7) {
                    for (int i = 7; i < split.length; i++) {
                        if (i % 2 != 0) {
                            long t = m3u8Index(split[i]);
                            if ((t - index) == 0) {
                                System.out.println("t = " + t);
                                index++;
                                down.offer(split[i]);
                            } else if ((t - index) > 0) {
                                // 说明断流 申请重新开流
                                index=t;
                                down.offer("1");
                            }
                        }
                    }
                }
            } else {
                down.offer("-1");
                break;
            }
         /*   try {
                *//*Thread.sleep(200);*//*

            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        return true;
    }

    /**
     * 开启下载线程
     */
    private void downloadStart() {
        new Thread(new Runnable() {
            BufferedInputStream bufferedInputStream = null;
            BufferedOutputStream bufferedOutputStream = null;
            long size = 0;
            @Override
            public void run() {
                CloseableHttpResponse response = null;
                byte[] buuf = new byte[1024];
                Date startTime=null;
                while (true) {
                    try {
                        String take = down.take();
                        if (take.equals("-1")) {
                            //下线
                            return;
                        }
                        if (take.equals("1")) {
                            //断开或者卡了的指示
                            startTime=new Date();
                            if (bufferedOutputStream!=null){
                                dispense(startTime);
                                HttpClientService.closeIO(bufferedOutputStream);
                            }
                            bufferedOutputStream = getBufferedOutputStream();
                            continue;
                        }
                        response = httpClientService.doResponse("127.0.0.1", 1021,take);
                        if (response == null) {
                            LOGGER.info("response is null:{}", user.getbId());
                            continue;
                        }
                        bufferedInputStream = new BufferedInputStream(response.getEntity().getContent());
                        int len = 0;
                        while ((len = bufferedInputStream.read(buuf)) != -1) {
                            size += len;
                            bufferedOutputStream.write(buuf, 0, len);

                            if (size >= MAX_SIZE) {
                               size=0;
                                HttpClientService.closeIO(bufferedOutputStream);
                                dispense(startTime);
                                bufferedOutputStream = getBufferedOutputStream();
                                //重置开始时间
                                startTime=new Date();
                            }
                        }
                        HttpClientService.closeIO(bufferedInputStream);
                        HttpClientService.closeIO(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                       // HttpClientService.closeIO(bufferedOutputStream);
                        HttpClientService.closeIO(bufferedInputStream);
                        HttpClientService.closeIO(response);
                    }
                }
            }
        }).start();
    }

    private BufferedOutputStream getBufferedOutputStream() throws FileNotFoundException {
        fileName = rootPath.concat(user.getUserName()).concat("_").concat(LocalDateTime.now().format(yyyyMMdd)).concat("_")
                .concat(user.getbId()).concat("_")
                .concat(Thread.currentThread().getName()).concat("_") + System.currentTimeMillis() + ".ts";
        return new BufferedOutputStream(new FileOutputStream(fileName));
    }

    /**
     * 查看这个url是否可用
     *
     * @param url
     * @return
     */
    private long m3u8Index(String url) {
        Matcher matcher = pattern.matcher(url);
        matcher.find();
        return Long.parseLong(matcher.group(1));

    }

    @Override
    public void run() {
        updateflage(1);
        try {
            record(user, url);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            down.offer("-1");
        }
        updateflage(0);
    }

    private void updateflage(int i) {
        ActionUser condition = new ActionUser();
        condition.setFlag(1);
        condition.setId(user.getId());
        actionUserMapper.updateByPrimaryKeySelective(condition);
    }

    /**
     * 分发到队列中
     *
     * @param startTime
     */
    private void dispense(Date startTime) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.length() > 1) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilePath(file.getAbsolutePath());
                fileInfo.setEndTime(new Date());
                fileInfo.setStartTime(startTime);
                fileInfo.setCreateTime(new Date());
                fileInfo.setUpdateTime(fileInfo.getCreateTime());
                fileInfo.setUserId(user.getId());
                fileInfoMapper.insertSelective(fileInfo);
                JSONObject object = new JSONObject();
                object.put("file", fileInfo);
                object.put("user", user);
                linkedBlockingDeques.offer(object);
            }
        }
    }
}
