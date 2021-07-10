package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-07-10 9:56 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class FixedValueTableReturnDto implements Serializable {

    private Long deviceTableId;

    private String fileUuid;
}
