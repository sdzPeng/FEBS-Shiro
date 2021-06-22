package cc.mrbird.febs.business.dto;

import lombok.Data;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-06-17 1:50 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class DimensionDto implements Serializable {

    private Double value;

    private Float angle;

    private RealVector realVector;
}
