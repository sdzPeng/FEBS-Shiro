package cc.mrbird.febs.business.dto;

import com.alibaba.excel.annotation.ExcelProperty;
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
public class BimDto {

    /**
     * BIM编码
     */
    @ExcelProperty("BIM编码")
    private String bimCode;

    /**
     *  支柱编号
     */
    @ExcelProperty("支柱编号")
    private String pillarCode;

    /**
     *  支柱编号
     */
    @ExcelProperty("里程")
    private String mileage;

    @ExcelProperty("行别")
    private String line;

    @ExcelProperty("支柱型号")
    private String pillarModel;

    @ExcelProperty("基础型号")
    private String baseModel;

    @ExcelProperty("拉线")
    private String stayWire;

    @ExcelProperty("拉线基础")
    private String stayWireBase;

    @ExcelProperty("腕臂安装图号")
    private String wbazth;

    @ExcelProperty("接触网下锚")
    private String jcwxm;

    @ExcelProperty("中心锚结")
    private String zxmj;

    @ExcelProperty("区间/车站")
    private String region;

    @ExcelProperty("其他")
    private String other;

}
