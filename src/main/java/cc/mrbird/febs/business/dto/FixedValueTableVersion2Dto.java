package cc.mrbird.febs.business.dto;

import cc.mrbird.febs.business.entity.FixedValueVersion;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class FixedValueTableVersion2Dto implements Serializable {

    private Long fixedValueTableId;

    private Long fixedValueVersionId;

    private String name;

    private String resourceName;

    private Date createTime;

    private Long resourceId;

    private Date firstTime;

    private String contextPath;

    private String previewUrl;
}
