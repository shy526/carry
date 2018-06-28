package top.ccxh.carry.scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ccxh.carry.mapper.anno.FileInfoMapper;
import top.ccxh.carry.mapper.pojo.FileInfo;

import java.util.List;
import java.util.Map;

public interface FileInfoService {

    /**
     * 根据flag 查出所有数据,根据grop进行分组
     * @param flag
     * @return
     */
    Map<String, List<FileInfo>> selectFileInfoGropByflag(int flag);
    Map<String, List<FileInfo>> selectFileInfoGropByflagEq2();
   void insertFileInfo(FileInfo fileInfo);
    void bathUpdateFileInfoFalgById(int flag,List<FileInfo> ids);
    void bathFileInfoSucceed(List<FileInfo> ids);
    void bathFileInfoRepair(List<FileInfo> ids);
    void bathFileInfoError(List<FileInfo> ids);
}
