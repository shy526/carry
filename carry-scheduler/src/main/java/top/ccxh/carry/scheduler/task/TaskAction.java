package top.ccxh.carry.scheduler.task;

public interface TaskAction {
    void scan();
    void repairUpload();
    String getActionUrl(String roomId) throws Exception;
}
