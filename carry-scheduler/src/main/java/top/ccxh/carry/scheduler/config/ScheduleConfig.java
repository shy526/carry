package top.ccxh.carry.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import top.ccxh.common.utils.ThreadPoolUtil;

import java.util.concurrent.Executors;

/**
 *  为@Scheduled 注解配置为多线程模式
 * @author honey
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(5));
    }

}
