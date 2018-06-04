package top.ccxh.carry.scheduler.task.youtube;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.ccxh.carry.mapper.anno.ActionUserMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.common.service.HttpClientService;
import top.ccxh.common.utils.ThreadPoolUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class YouTubeAction {
    ThreadPoolExecutor threadPool = ThreadPoolUtil.getThreadPool();
    private final static String url = "http://www.youtube.com/get_video_info?&video_id=%s";
    private final static Logger LOGGER = LoggerFactory.getLogger(YouTubeAction.class);
    Pattern pattern = Pattern.compile("hlsvp=(.*).m3u8");
    @Autowired
    ActionUserMapper actionUserMapper;
    @Autowired
    private HttpClientService httpClientService;

    @Scheduled(cron = "10/1 * * * * ? ")
    public void scan() {
        CloseableHttpResponse response = null;
        ActionUser actionUser = new ActionUser();
        actionUser.setFlag(0);
        actionUser.setLinkType(1);
        List<ActionUser> select = actionUserMapper.select(actionUser);

        for (ActionUser user : select) {
            String s = httpClientService.doGet("127.0.0.1", 1021, String.format(url, user.getbId()));
            if (null != s && !"".equals(s)) {
                String m3u8 = getM3u8(s);
                s = httpClientService.doGet("127.0.0.1", 1021, m3u8);
                if (null != s && !"".equals(s)) {
                    String[] split = s.split("\\n");
                    if (split != null && split.length > 0) {

                       // threadPool.execute(new YouTubeRecord(split[split.length - 1], httpClientService,user,actionUserMapper));
                    }
                }
            }
            LOGGER.info("bid:{}没有开始直播", user.getbId());
        }
    }

    private String getM3u8(String str) {
        Matcher matcher = null;
        try {
            matcher = pattern.matcher(URLDecoder.decode(URLDecoder.decode(str, "utf-8"), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        if (matcher.find()) {
            return matcher.group(1).concat("m3u8");
        }
        return null;
    }
}
