package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 4:16 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class DeviceAttachDto implements Serializable {

    private Long deviceId;

    private String deviceName;

    private List<DeviceResourceDto> children;

}
