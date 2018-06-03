package top.ccxh.common.vo;
public enum SysStatus {
    OK(200),ERR(201),PARAMETERR(400);
    private Integer code;
    SysStatus(Integer code){
        this.code=code;
    }

    public Integer getCode() {
        return code;
    }
}
