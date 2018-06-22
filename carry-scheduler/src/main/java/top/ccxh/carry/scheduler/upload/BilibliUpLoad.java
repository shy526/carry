package top.ccxh.carry.scheduler.upload;

import com.alibaba.fastjson.JSONObject;
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
import java.text.SimpleDateFormat;
import java.util.List;
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
    private List<FileInfo> fileInfoList;
    private ActionUser user;
    private SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat yyyyMMddHH = new SimpleDateFormat("yyyy-MM-dd");

    public boolean upload(JSONObject object) {
        if (object.get("file") instanceof List) {
            fileInfoList = (List<FileInfo>) object.get("file");
        }
        if (object.get("user") instanceof ActionUser) {
            user = (ActionUser) object.get("user");
        }
        WebDriver driver = WebDriverHelp.getChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        WebDriverHelp.sleep(4);
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
        }, 4, "跳过教程0").action(() -> {
            //单机投稿
            driver.findElement(By.id("nav_upload_btn")).click();
        }, 1, "单机投稿").action(() -> {
            //切换ifrom
            driver.findElement(By.cssSelector(".cc-header")).isDisplayed();
            driver.findElement(By.cssSelector(".cc-nav-wrp")).isDisplayed();
            driver.switchTo().frame("videoUpload");
        }, 1, "切换frame").action(() -> {
            //上传文件 TODO: 需要修改为群体上传
           driver.findElement(By.xpath("//*[@name=\"file\"]")).sendKeys(fileInfoList.get(0).getFilePath());
        }, 2, "上传文件").action(() -> {
            //跳过教程
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div/div/div/div/div")).click();
        }, 2, "跳过教程1").action(() -> {
            //跳过教程
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[3]/div/div/div/div/div")).click();
        }, 2, "跳过教程2").action(() -> {
            //跳过教程
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[3]/div/div/div/div/div[1]")).click();
        }, 2, "跳过教程3").action(() -> {
            //跳过教程
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[3]/div/div/div/div/div[1]")).click();
        }, 2, "知道了").action(() -> {
            //点击转载
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[2]/div[3]/div[1]/div[4]/div[2]/div[2]/span[2]")).click();
        }, 1, "点击转载").action(() -> {
            //输入信息 转载说明
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[2]/div[3]/div[1]/div[4]/div[3]/div/div/input"))
                    .sendKeys(user.getUserName().concat("直播实况:").concat(yyyyMMddHHmmss.format(this.fileInfoList.get(0).getStartTime())).concat("-").concat(yyyyMMddHHmmss.format(this.fileInfoList.get(this.fileInfoList.size()-1).getEndTime()))+"[直播间地址:https://live.bilibili.com/"+user.getbId()+"]");
        }, 1, "转载说明").action(() -> {

            if (this.fileInfoList.size()>1){

                //过滤文件1
                for (int i=1;i<this.fileInfoList.size();i++){
                    //获取板块
                    WebElement element = driver.findElement(By.cssSelector(".file-list-v2-container"));
                    WebElement upload = element.findElement(By.cssSelector(".webuploader-element-invisible"));
                    WebDriverHelp.sleep(1);
                    upload.sendKeys(fileInfoList.get(i).getFilePath());
                }

            }
        }, 1, "批量上传").action(() -> {
            //清楚标题
            WebElement element = driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[2]/div[3]/div[1]/div[8]/div[2]/div/div/input"));
            element.sendKeys("1");
            element.clear();
        }, 2, "清空标题").action(() -> {
            //填入标题
            WebElement element = driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[2]/div[3]/div[1]/div[8]/div[2]/div/div/input"));
            element.clear();
            element.sendKeys(user.getUserName().concat("_").concat(yyyyMMddHH.format(fileInfoList.get(0).getStartTime())).concat("直播实况"));
        }, 1, "添加标题").action(() -> {

            driver.findElement(By.xpath("//*[@id=\"type-list-v2-container\"]/div[2]/div/div")).click();
            //分区版本
            WebElement parren = driver.findElement(By.xpath("//*[@id=\"type-list-v2-container\"]/div[2]/div/div[2]"));
            //分割选项
            List<WebElement> elements = parren.findElements(By.cssSelector(".drop-cascader-pre-item"));

            for (WebElement element : elements) {
                if (element.getText().equals("生活")) {
                    element.click();
                    List<WebElement> subs = parren.findElement(By.cssSelector(".drop-cascader-list-wrp"))
                            .findElements(By.cssSelector(".item-main"));
                    for (WebElement sub : subs) {
                        if (sub.getText().equals("其他")) {
                            //筛选其他
                            sub.click();
                            break;
                        }
                    }
                    break;
                }
            }
            // WebElement element1 = element.findElement();
        }, 1, "生活分区选择asmr").action(() -> {
            //视屏简介
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[2]/div[3]/div[1]/div[12]/div[2]/div/textarea"))
                    .sendKeys(user.getUserName().concat(yyyyMMddHHmmss.format(this.fileInfoList.get(0).getStartTime())).concat("-")
                            .concat(yyyyMMddHHmmss.format(this.fileInfoList.get(this.fileInfoList.size()-1).getEndTime())).concat("直播实况"));
            actions.sendKeys(Keys.ENTER).build().perform();
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[2]/div[3]/div[1]/div[12]/div[2]/div/textarea"))
                    .sendKeys("乱入连接:https://space.bilibili.com/305768814");

        }, 1, "视屏简介").action(() -> {
            //添加标签
            driver.findElement(By.xpath("//*[@id=\"content-tag-v2-container\"]/div[2]/div/div[2]/input")).sendKeys("asmr");
            actions.sendKeys(Keys.ENTER).build().perform();
        }, 1, "添加asmr标签").action(() -> {
            //添加标签
            driver.findElement(By.xpath("//*[@id=\"content-tag-v2-container\"]/div[2]/div/div[2]/input")).sendKeys("直播实况");
            actions.sendKeys(Keys.ENTER).build().perform();
        }, 1, "添加直播实况标签").action(() -> {
            //添加标签
            driver.findElement(By.xpath("//*[@id=\"content-tag-v2-container\"]/div[2]/div/div[2]/input")).sendKeys(user.getUserName());
            actions.sendKeys(Keys.ENTER).build().perform();
        }, 1, "添加用户名标签").action(() -> {
            String flag = null;
            String load = null;
            //上传计数器
            int loadc=0;
            while (true) {
                loadc=0;
                try {
                    List<WebElement> elements = driver.findElement(By.cssSelector(".file-list-v2-container")).findElement(By.cssSelector(".file-list-v2-wrp"))
                            .findElements(By.cssSelector(".item-upload-info"));
                    for (WebElement element:elements){
                        flag=null;
                        flag = element.getText();
                        if ("上传完成".equals(flag)) {
                            loadc++;
                            continue;
                        } else if (null==flag&&"".equals(flag)) {
                            LOGGER.info("获取异常");
                            return;
                        } else  if ("上传错误".equals(flag)){
                            //TODO:此处有bug
                            loadc++;
                            continue;
                        }else {
                            WebDriverHelp.sleep(2);
                            LOGGER.info("{},{}",this.fileInfoList.get(0).getGroupId(), flag);
                        }
                    }
                    if (loadc<this.fileInfoList.size()){
                        LOGGER.info("{},总共{}个,还有{}个未上传",this.fileInfoList.get(0).getGroupId(),this.fileInfoList.size(),this.fileInfoList.size()-loadc);
                    }else{
                        return;
                    }
                    WebDriverHelp.sleep(2);
                } catch (Exception e) {
                    LOGGER.info("无法确认上传");
                    break;
                }
            }
        }, 1, "上传完成").action(() -> {
            //发布
            driver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[2]/div[3]/div[5]/span[1]")).click();
        }, 5, "发布");
        WebDriverHelp.printscreen(fileRoot.concat("/png/").concat(this.fileInfoList.get(0).getGroupId()).concat("_").concat(System.currentTimeMillis() + ".png"));
        action.action(() -> {
            String text = driver.findElement(By.xpath("//*[@id=\"app\"]/div/div[3]/div[3]/a[1]")).getText();
            if (text.equals("查看稿件")) {
                for (FileInfo file:this.fileInfoList){
                    this.fileInfoMapper.updateBathFileInfoByid(1,fileInfoList);
                }

            } else {
                for (FileInfo file:this.fileInfoList){
                    Integer flag = file.getFlag();
                    if (flag == null || flag == 0) {
                        this.fileInfoMapper.updateBathFileInfoByid(2,fileInfoList);
                       //上传不成功
                    } else {
                        this.fileInfoMapper.updateBathFileInfoByid(4,fileInfoList);
                       //补交不成功
                    }
                }

                WebDriverHelp.zclose();
            }
        }, 1, "查看稿件");
        WebDriverHelp.close();
        return true;
    }

    class Action {
        public Action action(motion m, int i, String msg) {
            Long start = System.currentTimeMillis();
            try {
                m.motiona();
            } catch (Exception e) {
                LOGGER.info("{}:动作执行异常:{}",msg,e.getClass().getSimpleName());
                WebDriverHelp.printscreen(fileRoot.concat("/png/").concat(fileInfoList.get(0).getGroupId()).concat("_").concat(System.currentTimeMillis() +msg+ "error.png"));
                return this;
            }
            LOGGER.info("{},动作执行时间:{}",msg, System.currentTimeMillis() - start);
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

    private void updateflag(FileInfo file,int i) {
            FileInfo filex = new FileInfo();
            filex.setId(file.getId());
            filex.setFlag(i);
            fileInfoMapper.updateByPrimaryKeySelective(filex);
    }
}
