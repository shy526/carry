package top.ccxh.common.mapper;

import org.apache.ibatis.annotations.SelectProvider;
import tk.mybatis.mapper.common.BaseMapper;

/**
 * 集成通用mapper的crud接口
 * 可扩展通用方法
 * @author shaw
 */
public interface SysMapper<T> extends BaseMapper<T> {
	
	//测试通用mapper的使用原理  编辑测试方法
	@SelectProvider(type = SysMapperProvider.class, method = "dynamicSQL")
	 int findMapperCount();


}
