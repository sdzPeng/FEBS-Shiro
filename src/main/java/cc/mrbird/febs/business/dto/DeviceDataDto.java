package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-30 4:20 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class DeviceDataDto implements Serializable {

    private Long deviceDataId;

    private String deviceKey;

    private String deviceValue;

    private String deviceResourceName;

    private String deviceName;

    private String desc;

    private String direction;
}
