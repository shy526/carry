package top.ccxh.common.mapper;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.provider.SpecialProvider;

import javax.persistence.Table;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 通用mapper扩展接实体类
 * @author shaw
 */
public class SysMapperProvider extends SpecialProvider {

    private static final Class<?>[] Type = null;


	public SysMapperProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    /**
     * 	测试通用Mapper的代码
     * MappedStatement Mybatis自己的内置对象
     */
    public SqlNode  findMapperCount(MappedStatement ms){
    	//1.获取方法调用的全路径   com.jt.manage.service.ItemServiceImpl.itemMapper.findMapperCount()
    	String path = ms.getId();
    	
    	//2.获取ItemMapper的路径   com.jt.manage.service.ItemServiceImpl.itemMapper
    	String mapperPath = path.substring(0, path.lastIndexOf("."));
    	
    	try {
    		//3.获取ItemMapper类型     通过反射获取对象类型
			Class<?> targetClass = Class.forName(mapperPath);
			
			//4.获取当前类型所继承的全部接口
			Type[]  types = targetClass.getGenericInterfaces();
			
			//5.获取Sysmapper
			Type type = types[0];
			
			//6.判断type类型是否为泛型接口    ParameterizedType   Item  List<sdf>
			if(type instanceof ParameterizedType) {
				//当前type为一个泛型
				//6.获取泛型类型
				ParameterizedType superType = (ParameterizedType) type;
				
				//7.获取泛型的参数
				Type[] argsType = superType.getActualTypeArguments();
				
				//8.获取一个参数  Item.Class
				Class<?> targetArgsCalss = (Class<?>) argsType[0];
				
				//9.判断是否还有@Table注解
				if(targetArgsCalss.isAnnotationPresent(Table.class)){
					
					//获取对象上的注解
					Table table = targetArgsCalss.getAnnotation(Table.class);
					
					//获取表名
					String tableName = table.name();
					
					String sql = "select count(*) from " + tableName;
					
					SqlNode sqlNode = new StaticTextSqlNode(sql);
					
					return sqlNode;
				}
				
			}
			
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
    	return null;
    }
    
    
    
    
    
    
    
    
    
    
    
    

}
