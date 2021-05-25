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
 * @date: 2021-05-23 2:59 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_fixed_value_meta")
@Excel("定值元数据表")
public class FixedValueMeta implements Serializable {

    @TableId(value = "FIXED_VALUE_META_ID", type = IdType.AUTO)
    private Long fixedValueMetaId;

    @TableField("CODE")
    @ExcelField(value = "编码", required = true, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("编码")
    private String code;

    @TableField("NAME")
    @ExcelField(value = "名称", required = true, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("名称")
    private String name;

    @TableField("PARENT_CODE")
    @ExcelField(value = "父编码", required = true, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("父编码")
    private String parentCode;

    @TableField("FIXED_VALUE_VERSION_ID")
    private Long fixedValueVersionId;
}
