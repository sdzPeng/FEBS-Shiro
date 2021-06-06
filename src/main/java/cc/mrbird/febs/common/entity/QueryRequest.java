package cc.mrbird.febs.common.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author MrBird
 */
@Data
@ToString
public class QueryRequest implements Serializable {

    private static final long serialVersionUID = -4869594085374385813L;
    // 当前页面数据量
//    @ApiModelProperty(value = "页码")
    private int pageSize = 10;
    // 当前页码
//    @ApiModelProperty(value = "页索引")
    private int pageNum = 1;
    // 排序字段
//    @ApiModelProperty(value = "排序字段")
    private String field;
    // 排序规则，asc升序，desc降序
//    @ApiModelProperty(value = "排序字段")
    private String order;
}
