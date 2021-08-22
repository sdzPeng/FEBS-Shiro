package cc.mrbird.febs.business.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-26 12:52 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class DeviceDto implements Serializable {

    @ExcelProperty("故障时间")
    private String failureTime;

    @ExcelProperty("被控站名称")
    private String siteName;

    @ExcelProperty("设备名称")
    private String deviceName;

    @ExcelProperty("故障序号")
    private String failureNum;

    @ExcelProperty("故障内容")
    private String content;
}
