package cc.mrbird.febs.business.dto;

import lombok.Data;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-06-17 1:50 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class DimensionDto implements Serializable {

    private Double value;

    private Float angle;

    private RealVector realVector;
}
