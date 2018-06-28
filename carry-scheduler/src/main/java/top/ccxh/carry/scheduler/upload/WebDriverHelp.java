package top.ccxh.carry.scheduler.upload;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;


public class WebDriverHelp {
    private static WebDriver webDriver;
    public static long createTime=0;
    private static   WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("no-sandbox");
        String path = "/home/project/carry/chromedriver";
        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
            //rpath = "F:\\javap\\carry\\carry-scheduler\\src\\main\\resources\\driver\\chromedriver.exe";
            path = "D:\\chromedriver.exe";
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new NullPointerException("没有这个文件");
        }
        if (!file.canExecute()) {
            //设置执行权
            file.setExecutable(true);
        }
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
        WebDriverHelp.createTime=System.currentTimeMillis();
        return new ChromeDriver(options);

    }
    public static WebDriver getChromeDriver(){
        if(WebDriverHelp.webDriver==null){
            WebDriverHelp.webDriver=createChromeDriver();
            //1280*720
            webDriver.manage().window().setSize(new Dimension(1280, 720));
            return webDriver;
        }
        return WebDriverHelp.webDriver;
    }
    public static void printscreen(String filePath) {
        File srcFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
        //利用FileUtils工具类的copyFile()方法保存getScreenshotAs()返回的文件对象。
        try {
            FileUtils.copyFile(srcFile, new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sleep(int i) {
        try {
            Thread.sleep(i == 0 ? 1000 : i * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static  void close() {
        boolean t=(System.currentTimeMillis()-createTime)>(1000*60*30);
        if (WebDriverHelp.webDriver!=null&&t){
            WebDriverHelp.zclose();
        }
    }

    public static void zclose() {
        webDriver.close();
        webDriver.quit();
        WebDriverHelp.webDriver = null;
    }
}
