package top.ccxh.carry.scheduler.upload;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.ccxh.carry.mapper.anno.CookieMapper;
import top.ccxh.carry.mapper.anno.FileInfoMapper;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.CookiePojo;
import top.ccxh.carry.mapper.pojo.FileInfo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class BilibliUpLoad {
    private static final String url = "https://member.bilibili.com/v2#/home";
    private static final Logger LOGGER = LoggerFactory.getLogger(BilibliUpLoad.class);
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    CookieMapper cookieMapper;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    FileInfoMapper fileInfoMapper;
    @Value("${file.root}")
    private String fileRoot;
    private FileInfo file;
    private ActionUser user;
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public boolean upload(JSONObject object) {

        if (object.get("file") instanceof FileInfo) {
            file = (FileInfo) object.get("file");

        }

        if (object.get("user") instanceof ActionUser) {
            user = (ActionUser) object.get("user");
        };
        WebDriver driver = WebDriverHelp.getChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        WebDriverHelp.sleep(5);
        if (!setCookie(driver)) {
            return false;
        }
        Action action = new Action();
        Actions actions = new Actions(driver);
        action.action(() -> {
            //跳过教程
            By xpath = By.xpath("//*[@id=\"root\"]/div[1]/div[1]/div/div/div/div[2]");
            driver.findElement(xpath).click();
            ;
        }, 5,"跳过教程").action(() -> {
            //单机投稿
            driver.findElement(By.id("nav_upload_btn")).click();
        }, 1,"单机投稿").action(() -> {
            //切换ifrom
            driver.switchTo().frame("videoUpload");
        }, 1,"切换frame").action(() -> {
            //上传文件
            driver.findElement(By.xpath("//*[@name=\"file\"]")).sendKeys(file.getFilePath());
        }, 2,"上传文件").action(() -> {
            //跳过教程
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div/div/div/div/div")).click();
        }, 2,"跳过教程").action(() -> {
            //点击转载
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[1]/div[2]/div[2]")).click();
        }, 1,"点击转载").action(() -> {
            //输入信息 转载说明
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[1]/div[3]/div/div[1]/div/input"))
                    .sendKeys(user.getUserName().concat("_").concat(yyyyMMdd.format(file.getStartTime())).concat("-").concat(yyyyMMdd.format(file.getEndTime())).concat("直播实况"));
        }, 1,"转载说明").action(() -> {
            //清楚标题
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[3]/div[2]/div/div/input")).sendKeys("1");
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[3]/div[2]/div/div/input")).clear();
        }, 2,"清空标题").action(() -> {
            //填入标题
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[3]/div[2]/div/div/input")).clear();
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[3]/div[2]/div/div/input")).sendKeys(user.getUserName().concat("_").concat(yyyyMMdd.format(file.getStartTime())).concat("-").concat(yyyyMMdd.format(file.getEndTime())).concat("直播实况"));
        }, 1,"添加标题").action(() -> {
            //选择生活分区
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[2]/div[2]/div[1]/div[12]/div[1]")).click();
        }, 1,"生活分区").action(() -> {
            //选择生活分区下的asmr
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[2]/div[2]/div[1]/div[12]/div[2]/div[9]")).click();
        }, 1,"选择asmr").action(() -> {
            //
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[6]/div[1]/div[2]/div")).click();
        }, 1,"同步").action(() -> {
            //视屏简介
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[5]/div/div/div[2]/div[1]/textarea"))
                    .sendKeys(user.getUserName().concat(yyyyMMdd.format(file.getStartTime())).concat("-").concat(yyyyMMdd.format(file.getEndTime())).concat("直播实况"));
        }, 1,"视屏简介").action(() -> {
            //添加标签
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[4]/div[2]/div/div[2]/div[2]/div[1]/input")).sendKeys("asmr");
            actions.sendKeys(Keys.ENTER).build().perform();
        }, 1,"添加标签").action(() -> {
            //添加标签
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[2]/div[1]/div[4]/div[2]/div/div[2]/div[2]/div[2]/input")).sendKeys("直播实录");
            actions.sendKeys(Keys.ENTER).build().perform();
        }, 1,"添加标签").action(() -> {
            String flag = null;
            while (true) {
                try {
                    flag = driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[1]/div[1]/div/div/div[2]/div[1]/div[2]")).getText();
                    if ("上传完成".equals(flag)) {
                        break;
                    }else if ("正在上传".equals(flag)){
                        WebDriverHelp.sleep(10);
                    }
                    else break;
                } catch (Exception e) {
                }
            }
        }, 1,"上传完成").action(() -> {
            //发布
            driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[2]/div[3]/div[2]/div[3]/div[1]")).click();
        }, 5,"发布");
        WebDriverHelp.printscreen(fileRoot.concat("/png/").concat(new File(file.getFilePath()).getName()).concat("_").concat(System.currentTimeMillis() + ".png"));
        action.action(() -> {
            String text = driver.findElement(By.xpath("//*[@id=\"item\"]/div/div[3]/div[3]/a")).getText();
            if (text.equals("查看稿件")) {
                    updateflag(1); //上传
            }else {
                Integer flag = file.getFlag();
                if (flag==null||flag==0){
                    updateflag(2); //上传成功
                }else {
                    updateflag(4); //补交成功
                }
                WebDriverHelp.zclose();
            }
        }, 1,"查看稿件");
        WebDriverHelp.close();
        return true;
    }

    class Action {
        public Action action(motion m, int i,String msg) {
            Long start = System.currentTimeMillis();
            try {
                m.motiona();
            } catch (Exception e) {
                LOGGER.info("动作执行异常:{}", msg);
                return this;
            }
            LOGGER.info("动作执行时间:{}", System.currentTimeMillis() - start);
            WebDriverHelp.sleep(i);
            return this;
        }
    }

    interface motion {
        void motiona();
    }

    /**
     * 添加驱动.知道知道有效的cookiew
     *
     * @param driver
     * @return
     */
    private boolean setCookie(WebDriver driver) {
        driver.get(url);
        CookiePojo c = new CookiePojo();
        c.setFlag(0);
        List<CookiePojo> select = cookieMapper.select(c);
        if (select.size() < 1) {
            LOGGER.info("无可用Cookie");
            return false;
        }
        String str = select.get(select.size() - 1).getCookie();
        String[] split = str.split(";");
        driver.manage().deleteAllCookies();
        for (String str1 : split) {
            String[] split1 = str1.split("=");
            Cookie cookie = new Cookie(split1[0].trim(), split1[1].trim(), ".bilibili.com", "/", null);
            driver.manage().addCookie(cookie);
        }
        driver.get(url);
        if (!driver.getCurrentUrl().equalsIgnoreCase(url)) {
            CookiePojo c1 = new CookiePojo();
            c1.setId(c.getId());
            c1.setFlag(1);
            cookieMapper.updateByPrimaryKey(c1);
            LOGGER.info("Cookie过期");
            return setCookie(driver);
        }
        return true;
    }

    private void updateflag(int i) {
        FileInfo filex = new FileInfo();
        filex.setId(file.getId());
        filex.setFlag(i);
        fileInfoMapper.updateByPrimaryKeySelective(filex);
    }
}
