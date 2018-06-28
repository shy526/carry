package top.ccxh.carry.scheduler.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ccxh.carry.mapper.anno.FileInfoMapper;
import top.ccxh.carry.mapper.pojo.FileInfo;
import top.ccxh.carry.scheduler.service.FileInfoService;

import java.util.*;

@Service
public class FileInfoServiceImpl implements FileInfoService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
   private FileInfoMapper fileInfoMapper;
    /**
     *待补交
     */
    private static final int REPAIR=2;
    /**
     *提交成功
     */
    private static final int SUCCEED =1;
    /**
     *补交中
     */
    private static final int PROCEED=3;
    /**
     *补交失败
     */
    private static final int ERROE=2;

    @Override
    public Map<String, List<FileInfo>> selectFileInfoGropByflag(int flag) {
        FileInfo condition =new FileInfo();
        //test 5 2
        condition.setFlag(flag); //补交也是以分p方式补交
        List<FileInfo> result = fileInfoMapper.select(condition);
        return groupFileinfo(result);
    }

    @Override
    public Map<String, List<FileInfo>> selectFileInfoGropByflagEq2() {
        return selectFileInfoGropByflag(REPAIR);
    }

    @Override
    public void insertFileInfo(FileInfo fileInfo) {
        fileInfo.setCreateTime(new Date());
        fileInfo.setUpdateTime(fileInfo.getCreateTime());
        fileInfoMapper.insertSelective(fileInfo);
    }

    @Override
    public void bathUpdateFileInfoFalgById(int flag,List<FileInfo> ids) {
        fileInfoMapper.updateBathFileInfoByid(flag,ids);
    }
    public void bathFileInfoSucceed(List<FileInfo> ids) {
        this.bathUpdateFileInfoFalgById(SUCCEED,ids);
    }

    @Override
    public void bathFileInfoRepair(List<FileInfo> ids) {
        this.bathUpdateFileInfoFalgById(REPAIR,ids);
    }

    @Override
    public void bathFileInfoError(List<FileInfo> ids) {
        this. bathUpdateFileInfoFalgById(ERROE,ids);

    }


    /**
     * 对结果集进行分组,并修改标记
     * @param select
     * @return
     */
    private Map<String, List<FileInfo>> groupFileinfo(List<FileInfo> select) {
        List<JSONObject> objects=new ArrayList<>();
         Map<String,List<FileInfo>> groupList=new HashMap<String,List<FileInfo>>();
        for (FileInfo fileInfo:select){
            List<FileInfo> fileInfos = groupList.get(fileInfo.getGroupId());
            if (fileInfos==null){
                fileInfos= new ArrayList<>();
            }
            fileInfos.add(fileInfo);
            groupList.put(fileInfo.getGroupId(),fileInfos);
        }

        if (select.size()>0){
            this.fileInfoMapper.updateBathFileInfoByid(3,select);
        }
        return groupList;
    }
}
