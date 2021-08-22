package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-06-17 1:29 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class CurrentValue implements Serializable {

    private Double value;

    private Integer direction;
}
