package cc.mrbird.febs.business.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-09-30 2:43 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class DeviceTableDto implements Serializable {
    private Long deviceTableId;
    @TableField(value = "NAME")
    private String name;

    @TableField(value = "CREATE_TIME")
    private Date createTime;

    @TableField(value = "RESOURCE_ID")
    private Long resourceId;

    @TableField(value = "FIXED_VALUE_VERSION_ID")
    private Long fixedValueVersionId;

    @TableField(value = "FAILURE_TIME")
    @ExcelProperty("故障时间")
    private String failureTime;

    @TableField(value = "SITE_NAME")
    @ExcelProperty("被控站名称")
    private String siteName;

    @TableField(value = "DEVICE_NAME")
    @ExcelProperty("设备名称")
    private String deviceName;
}
