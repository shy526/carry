package top.ccxh.carry.scheduler.task;

import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingDeque;

@Component
public class DequeManger {
    private  LinkedBlockingDeque<Object> linkedBlockingDeques=new LinkedBlockingDeque<Object>() ;
    public LinkedBlockingDeque<Object>  getDeque(){
        return this.linkedBlockingDeques;
    }
    public int  getDequeSize(){
        return this.linkedBlockingDeques.size();
    }

}
