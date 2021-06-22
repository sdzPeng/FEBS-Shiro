package cc.mrbird.febs.business.dto;

import cc.mrbird.febs.business.entity.FixedValueVersion;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 3:47 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class FixedValueTableDto implements Serializable {

    private Long fixedValueTableId;

    private String name;

    private Date createTime;

    private List<FixedValueVersion> fixedValueVersionId;
}
