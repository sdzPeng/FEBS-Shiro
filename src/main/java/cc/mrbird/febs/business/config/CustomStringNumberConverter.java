package cc.mrbird.febs.business.config;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.excel.util.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.NumberToTextConverter;
import java.math.BigDecimal;
/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-06-12 8:35 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public class CustomStringNumberConverter implements Converter<String> {

    @Override
    public Class supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public String convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // If there are "DateTimeFormat", read as date
        if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
            return com.alibaba.excel.util.DateUtils.format(
                    HSSFDateUtil.getJavaDate(Double.parseDouble(cellData.getNumberValue().toString()),
                            contentProperty.getDateTimeFormatProperty().getUse1904windowing(), null),
                    contentProperty.getDateTimeFormatProperty().getFormat());
        }
        // If there are "NumberFormat", read as number
        if (contentProperty != null && contentProperty.getNumberFormatProperty() != null) {
            return NumberUtils.format(Double.parseDouble(cellData.getNumberValue().toString()), contentProperty);
        }

        // Excel defines formatting
        if (cellData.getDataFormat() != null) {
            if (DateUtil.isADateFormat(cellData.getDataFormat(), cellData.getDataFormatString())) {

                if(cellData.getDataFormatString().contains(":")){
                    return DateUtils.format(HSSFDateUtil.getJavaDate(Double.parseDouble(cellData.getNumberValue().toString()),
                            globalConfiguration.getUse1904windowing(), null));
                } else {
                    return DateUtils.format(HSSFDateUtil.getJavaDate(Double.parseDouble(cellData.getNumberValue().toString()),
                            globalConfiguration.getUse1904windowing(), null), "yyyy-MM-dd");
                }
            }  else if(contentProperty == null) {
                try{
                    // 百分比
                    if(cellData.getDataFormatString() != null && cellData.getDataFormatString().contains("%")){
                        return  new BigDecimal(Double.parseDouble(cellData.getNumberValue().toString())).multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString()  + "%";
                    } else if(cellData.getDataFormatString() != null && cellData.getDataFormatString().equals("General")){

                        //解决easyExcel 解析无 CLASS 对象时，Number to string  用String去接收数字，出现小数点等情况  方法一 会出现 数字位数失真的情况 ，即 excel 用公式计算数值后，只保留3位小数， 读取时 可能出现 直接去取保留前的N为小数的情况 建议使用方法二
//                     方法一   NumberFormat numberFormat = NumberFormat.getInstance();
//                        numberFormat.setMaximumFractionDigits(20);
//                        numberFormat.setGroupingUsed(false);
                        //  return numberFormat.format(cellData.getDoubleValue());
                        // 方法二
                        return   NumberToTextConverter.toText(Double.parseDouble(cellData.getNumberValue().toString()));
                    } else {
                        return   NumberToTextConverter.toText(Double.parseDouble(cellData.getNumberValue().toString()));
                        // return cellData.getDoubleValue().toString();
                    }
                } catch (Exception e) {
                    // 建议 统一使用以下方法，可以解决数值格式问题
                    return   NumberToTextConverter.toText(Double.parseDouble(cellData.getNumberValue().toString()));
                    //   return NumberUtils.format(cellData.getDoubleValue(), contentProperty);
                }
            } else {
                return   NumberToTextConverter.toText(Double.parseDouble(cellData.getNumberValue().toString()));
                //  return NumberUtils.format(cellData.getDoubleValue(), contentProperty);
            }
        }
        // Default conversion number
//        NumberFormat numberFormat = NumberFormat.getInstance();
//        numberFormat.setMaximumFractionDigits(20);
//        numberFormat.setGroupingUsed(false);
//        return numberFormat.format(cellData.getDoubleValue());
        // 方法二
        return   NumberToTextConverter.toText(Double.parseDouble(cellData.getNumberValue().toString()));
        // return NumberUtils.format(cellData.getDoubleValue(), contentProperty);
    }

    @Override
    public CellData convertToExcelData(String value, ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) {
        return new CellData(Double.valueOf(value));
    }
}