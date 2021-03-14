package red.fuyun.pojo.Do;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/4 13:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserInfoDo {
    private Integer id;
    private String tenantId;
    private String ua;
    private String cookie;
    private String studentNo;
    private String origin;
    private String cpdailyInfo;
    private String AmpCookie;

}
