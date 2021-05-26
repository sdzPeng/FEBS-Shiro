package cc.mrbird.febs.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import lombok.Data;
import java.util.Date;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-24 11:45 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_fixed_value_table")
@Excel("定值表格信息")
public class FixedValueTable {

    @TableId(value = "FIXED_VALUE_TABLE_ID", type = IdType.AUTO)
    private Long fixedValueTableId;

    @TableField(value = "NAME")
    private String name;

    @TableField(value = "CREATE_TIME")
    private Date createTime;

}
