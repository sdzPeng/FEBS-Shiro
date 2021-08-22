package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-30 6:18 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class DeviceDescDto implements Serializable {

    private String name;

    private List<DeviceResourceDto> data;
}
