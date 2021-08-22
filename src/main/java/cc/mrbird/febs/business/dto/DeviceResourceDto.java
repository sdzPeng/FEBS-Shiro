package cc.mrbird.febs.business.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-30 4:18 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class DeviceResourceDto implements Serializable {

    private Long deviceResourceId;

    private String deviceResourceName;

    private List<DeviceDataDto> children;
}
