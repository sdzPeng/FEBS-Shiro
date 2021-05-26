package cc.mrbird.febs.business.entity;

import cc.mrbird.febs.common.converter.TimeConverter;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-23 2:42 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_fixed_value_version")
public class FixedValueVersion implements Serializable {

    @TableId(value = "FIXED_VALUE_VERSION_ID", type = IdType.AUTO)
    private Long fixValueVersionId;

    @TableField("VERSION")
    private String version;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("DIRECTION")
    private String direction;

    @TableField("FIXED_VALUE_TABLE_ID")
    private Long fixedValueTableId;
}
