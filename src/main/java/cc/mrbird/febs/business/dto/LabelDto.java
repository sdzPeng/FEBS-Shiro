package cc.mrbird.febs.business.dto;

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
public class LabelDto {

    /**
     * BIM编码
     */
    @ExcelProperty("行别")
    private String line;

    /**
     *  支柱编号
     */
    @ExcelProperty("支柱号")
    private String pillarNum;

    /**
     *  支柱编号
     */
    @ExcelProperty("里程")
    private String mileage;

    @ExcelProperty("标签")
    private String label;

    @ExcelProperty("附加信息")
    private String addition;

    @ExcelProperty("区间/车站")
    private String region;

    @ExcelProperty("偏移量")
    private String offsetNum;

}
