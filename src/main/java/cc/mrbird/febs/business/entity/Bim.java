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
@TableName("t_bim_info")
@Excel("bim表信息")
public class Bim {

    @TableId(value = "BIM_ID", type = IdType.AUTO)
    private Long bimId;

    /**
     * BIM编码
     */
    @TableField(value = "BIM_CODE")
    @ExcelField(value = "BIM编码", required = false, maxLength = 40,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("BIM编码")
    private String bimCode;

    /**
     *  支柱编号
     */
    @TableField(value = "PILLAR_CODE")
    @ExcelField(value = "支柱编码", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("支柱编码")
    private String pillarCode;

    /**
     *  支柱编号
     */
    @TableField(value = "MILEAGE")
    @ExcelField(value = "里程", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("里程")
    private String mileage;

    @TableField(value = "LINE")
    @ExcelField(value = "行别", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("行别")
    private String line;

    @TableField(value = "PILLAR_MODEL")
    @ExcelField(value = "支柱型号", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("支柱型号")
    private String pillarModel;

    @TableField(value = "BASE_MODEL")
    @ExcelField(value = "基础型号", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("基础型号")
    private String baseModel;

    @TableField(value = "STAY_WIRE")
    @ExcelField(value = "拉线", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("拉线")
    private String stayWire;

    @TableField(value = "STAY_WIRE_BASE")
    @ExcelField(value = "拉线基础", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("拉线基础")
    private String stayWireBase;

    @TableField(value = "WBAZTH")
    @ExcelField(value = "腕臂安装图号", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("腕臂安装图号")
    private String wbazth;

    @TableField(value = "JCWXM")
    @ExcelField(value = "接触网下锚", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("接触网下锚")
    private String jcwxm;

    @TableField(value = "ZXMJ")
    @ExcelField(value = "中心锚结", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("中心锚结")
    private String zxmj;

    @TableField(value = "REGION")
    @ExcelField(value = "区间/车站", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("区间/车站")
    private String region;

    @TableField(value = "OTHER")
    @ExcelField(value = "其他", required = false, maxLength = 20,
            comment = "提示：必填，长度不能超过20个字符")
    @ExcelProperty("其他")
    private String other;

}
