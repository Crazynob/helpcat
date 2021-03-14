package red.fuyun.pojo.Do;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Time;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/6 16:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDo {
    private Integer id;
    private Integer uid;
    private Integer tid;
    private Time rateTaskBeginTime;
    private Time rateTaskEndTime;
    private Integer taskType;
    private Integer advise;
    private  String info;
    //0 未签到 1在线程池 2成功 3失败
    private Integer state;

}
