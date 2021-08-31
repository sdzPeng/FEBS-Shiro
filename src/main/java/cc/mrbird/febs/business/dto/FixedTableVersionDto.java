package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-08-30 9:08 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class FixedTableVersionDto implements Serializable {

    private Long fixedValueTableId;

    private Long fixedValueVersionId;
}
