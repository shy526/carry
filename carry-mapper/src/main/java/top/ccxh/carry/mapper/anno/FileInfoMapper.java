package top.ccxh.carry.mapper.anno;

import org.apache.ibatis.annotations.Param;
import top.ccxh.carry.mapper.pojo.ActionUser;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.common.mapper.SysMapper;

import java.util.List;

public interface FileInfoMapper extends SysMapper<FileInfo> {
    int updateBathFileInfoByid(@Param("flag") Integer flag, @Param("fileInfos")List<FileInfo> FileInfos);
}
