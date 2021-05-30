package cc.mrbird.febs.business.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 6:45 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class KeyValueResult implements Serializable {

    public KeyValueResult() {}

    public KeyValueResult(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    private String key;

    private Object value;
}
