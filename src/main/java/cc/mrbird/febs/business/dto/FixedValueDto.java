package cc.mrbird.febs.business.dto;

import lombok.Data;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-24 8:50 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
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
