package top.ccxh.carry.scheduler;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@EnableScheduling
@MapperScan("top.ccxh.carry.mapper.anno")
@ComponentScan("top.ccxh.common.service")
@ComponentScan("top.ccxh.carry.scheduler")
public class SchedulerAction {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(SchedulerAction.class);
    }
}
