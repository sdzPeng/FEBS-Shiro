package cc.mrbird.febs.business.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;
import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-22 2:29 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_fixed_value")
@Excel("部门信息表")
public class FixedValue implements Serializable {

    @TableId(value = "FIXED_VALUE_ID", type = IdType.AUTO)
    private Long fixValueId;

    /**
     * 序列号
     */
    @TableField(value = "SERIAL_NUMBER")
    @ExcelField(value = "序号", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("序号")
    private Long serialNumber;

    /**
     * 定值名称
     */
    @TableField(value = "NAME")
    @ExcelField(value = "定值名称", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("定值名称")
    private String name;

    @TableField(value = "UNIT")
    @ExcelField(value = "单位", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("单位")
    private String unit;

    /**
     * 范围
     */
    @TableField(value = "BOUNDARY")
    @ExcelField(value = "范围", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("范围")
    private String boundary;

    /**
     * 缺省值
     */
    @TableField(value = "DEFAULT_VALUE")
    @ExcelField(value = "缺省值", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("缺省值")
    private String defaultValue;

    /**
     * 召唤值
     */
    @TableField(value = "SUMMON_VALUE")
    @ExcelField(value = "召唤值", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("召唤值")
    private String summonValue;

    /**
     * 新定值
     */
    @TableField(value = "NEW_VALUE")
    @ExcelField(value = "新定值", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("新定值")
    private String newValue;

    @TableField("FIXED_VALUE_VERSION_ID")
    private Long fixedValueVersionId;

}
