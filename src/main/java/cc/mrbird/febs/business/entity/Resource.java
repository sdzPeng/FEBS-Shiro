package cc.mrbird.febs.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-28 12:38 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
@TableName("t_resource")
public class Resource implements Serializable {

    @TableId(value = "RESOURCE_ID", type = IdType.AUTO)
    private Long resourceId;

    @TableField(value = "FILE_NAME")
    private String fileName;

    @TableField(value = "FILE_LENGTH")
    private Long fileLength;

    @TableField(value = "SUFFIX")
    private String suffix;

    @TableField(value = "CONTENT_TYPE")
    private String contentType;

    @TableField(value = "UUID")
    private String uuid;

}
