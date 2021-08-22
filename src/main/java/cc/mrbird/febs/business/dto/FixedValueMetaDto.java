package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-23 8:57 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class FixedValueMetaDto implements Serializable {

    private String code;

    private String name;

    private String parentCode;
}
