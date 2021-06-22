package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.dto.CurrentValue;
import cc.mrbird.febs.business.dto.KeyValueResult;

import java.util.List;
import java.util.Map;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 1:11 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public interface ICalcService {
    List<KeyValueResult> analysisResult(Long deviceId);

    List<KeyValueResult> calcData(Long deviceId);

    Map<String, CurrentValue> currentDistMap(Long deviceId);
}
