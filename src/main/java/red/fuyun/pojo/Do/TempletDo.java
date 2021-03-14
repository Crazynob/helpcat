package red.fuyun.pojo.Do;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/6 13:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempletDo {
    private Integer id;
    private Integer uid;
    private String cpdailyExtension;
    private String data;
    private String longitude;
    private String latitude;
    private String model;
    private String signAddress;
    private String appVersion;
    private String systemName;
    private String deviceId;
    private String userId;
    private String signPhotoUrl;
    private String collectorPhotoUrl;
    private String attendancePhotoUrl;
    private Integer taskType;
}
