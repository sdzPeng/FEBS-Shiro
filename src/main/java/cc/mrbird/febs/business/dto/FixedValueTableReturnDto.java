package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-07-10 9:56 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class FixedValueTableReturnDto implements Serializable {

    private Long deviceTableId;

    private String fileUuid;
}
