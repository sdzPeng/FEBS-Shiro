package cc.mrbird.febs.business.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import lombok.Data;
import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-25 3:38 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_device")
@Excel("设备信息表")
public class Device implements Serializable {

    @TableId(value = "DEVICE_ID", type = IdType.AUTO)
    private Long deviceId;

//    @TableField(value = "SERIAL_NUMBER")
//    @ExcelProperty("记录号")
//    private Long serialNumber;

    @TableField(value = "FAILURE_TIME")
    @ExcelProperty("故障时间")
    private String failureTime;

    @TableField(value = "SITE_NAME")
    @ExcelProperty("被控站名称")
    private String siteName;

    @TableField(value = "DEVICE_NAME")
    @ExcelProperty("设备名称")
    private String deviceName;

    @TableField(value = "FAILURE_NUM")
    @ExcelProperty("故障序号")
    private String failureNum;

    @TableField(value = "CONTENT")
    @ExcelProperty("故障内容")
    private String content;

    @TableField(value = "FAILURE_PROFILE")
    private String failureProfile;

    @TableField(value = "FIXED_VALUE_VERSION_ID")
    private Long fixedValueVersionId;

}
