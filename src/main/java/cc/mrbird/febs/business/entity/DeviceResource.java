package cc.mrbird.febs.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import lombok.Data;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-25 8:05 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_device_resource")
@Excel("设备故障信息表")
public class DeviceResource {

    @TableId(value = "DEVICE_RESOURCE_ID", type = IdType.AUTO)
    private Long deviceResourceId;

    @TableId(value = "DEVICE_RESOURCE_NAME")
    private String deviceResourceName;

    @TableId(value = "DEVICE_ID")
    private Long deviceId;
}
