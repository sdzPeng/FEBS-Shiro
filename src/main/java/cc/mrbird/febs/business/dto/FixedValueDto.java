package cc.mrbird.febs.business.dto;

import lombok.Data;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-24 8:50 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class FixedValueDto {
    private Long serialNumber;
    private String name;
    private String unit;
    private String range;
    private String defaultValue;
    private String summonValue;
    private String newValue;
}
