package cc.mrbird.febs.business.dto;

import cc.mrbird.febs.business.entity.FixedValueVersion;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-30 3:47 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Data
public class FixedValueTableVersionDto implements Serializable {

    private Long fixedValueTableId;

    private String name;

    private Date createTime;

    private Long fixedValueVersionId;
}
