package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 4:20 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
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
