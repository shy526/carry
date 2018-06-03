package top.ccxh.common.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建线程池帮助类
 * @author honey
 */
public class ThreadPoolUtil {
    private static ThreadPoolConfig threadPoolConfig=new ThreadPoolConfig();
    static class ThreadPoolConfig{
        /**
         * 线程池的基本大小
         */
        private int corePoolSize = 10;
        /**
         * 线程池最大数量
         */
        private int maximumPoolSizeSize = 100;
        /**
         * 线程活动保持时间
         */
        private long keepAliveTime = 1;
        /**
         * 任务队列
         */
        private ArrayBlockingQueue workQueue = new ArrayBlockingQueue(10);

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaximumPoolSizeSize() {
            return maximumPoolSizeSize;
        }

        public void setMaximumPoolSizeSize(int maximumPoolSizeSize) {
            this.maximumPoolSizeSize = maximumPoolSizeSize;
        }

        public long getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public ArrayBlockingQueue getWorkQueue() {
            return workQueue;
        }

        public void setWorkQueue(ArrayBlockingQueue workQueue) {
            this.workQueue = workQueue;
        }
    }


    public static ThreadPoolExecutor getThreadPool(){
       return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(), threadPoolConfig.getMaximumPoolSizeSize(), threadPoolConfig.getKeepAliveTime(),
               TimeUnit.SECONDS,
               threadPoolConfig.getWorkQueue(), new ThreadFactory() {
           @Override
           public Thread newThread(Runnable r) {
               return new Thread(r);
           }
       });
    }
}
