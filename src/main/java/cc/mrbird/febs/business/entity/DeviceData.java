package cc.mrbird.febs.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import lombok.Data;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-25 6:45 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
@TableName("t_device_data")
@Excel("设备故障信息表")
public class DeviceData {

    @TableId(value = "DEVICE_DATA_ID", type = IdType.AUTO)
    private Long deviceDataId;

    @TableId(value = "DEVICE_KEY")
    private String deviceKey;

    @TableId(value = "DEVICE_VALUE")
    private String deviceValue;

    @TableId(value = "UNIT")
    private String unit;

    @TableId(value = "DEVICE_RESOURCE_ID")
    private Long deviceResourceId;

}
