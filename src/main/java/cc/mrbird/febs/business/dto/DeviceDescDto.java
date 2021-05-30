package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 6:18 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class DeviceDescDto implements Serializable {

    private String name;

    private List<DeviceResourceDto> data;
}
