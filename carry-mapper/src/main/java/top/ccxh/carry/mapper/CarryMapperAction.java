package top.ccxh.carry.mapper;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("top.ccxh.carry.mapper.anno")
public class CarryMapperAction {

	public static void main(String[] args) {
		SpringApplication.run(CarryMapperAction.class, args);
	}
}
