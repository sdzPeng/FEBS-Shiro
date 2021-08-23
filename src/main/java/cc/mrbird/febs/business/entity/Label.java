package cc.mrbird.febs.business.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-08-20 3:35 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
@TableName("t_label_info")
@Excel("label表信息")
public class Label {

    @TableId(value = "LABEL_ID", type = IdType.AUTO)
    private Long labelId;

    /**
     * BIM编码
     */
    @TableField(value = "LINE")
    @ExcelField(value = "行别", required = false, maxLength = 40,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("行别")
    private String line;

    /**
     *  支柱编号
     */
    @TableField(value = "PILLAR_NUM")
    @ExcelField(value = "支柱号", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("支柱号")
    private String pillarNum;

    /**
     *  支柱编号
     */
    @TableField(value = "MILEAGE")
    @ExcelField(value = "里程", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("里程")
    private String mileage;

    @TableField(value = "LABEL")
    @ExcelField(value = "标签", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("标签")
    private String label;

    @TableField(value = "ADDITION")
    @ExcelField(value = "附加信息", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("附加信息")
    private String addition;

    @TableField(value = "REGION")
    @ExcelField(value = "区间/车站", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("区间/车站")
    private String region;

    @TableField(value = "OFFSET_NUM")
    @ExcelField(value = "偏移量", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("偏移量")
    private String offsetNum;

}
