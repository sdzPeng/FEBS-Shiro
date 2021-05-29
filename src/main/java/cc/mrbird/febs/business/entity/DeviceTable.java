package cc.mrbird.febs.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-29 3:58 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_device_table")
@Excel("设备表")
public class DeviceTable implements Serializable {

    @TableId(value = "DEVICE_TABLE_ID", type = IdType.AUTO)
    private Long deviceTableId;

    @TableField(value = "NAME")
    private String name;

    @TableField(value = "CREATE_TIME")
    private Date createTime;

    @TableField(value = "RESOURCE_ID")
    private Long resourceId;

    @TableField(value = "FIXED_VALUE_VERSION_ID")
    private Long fixedValueVersionId;

}
