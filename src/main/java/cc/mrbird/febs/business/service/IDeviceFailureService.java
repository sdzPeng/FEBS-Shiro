package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.entity.DeviceData;
import cc.mrbird.febs.business.entity.DeviceTable;

import java.util.List;
import java.util.Map;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 7:47 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public interface IDeviceFailureService {
    Object getDevicesByDeviceTableId(Long deviceTableId);

    List<DeviceData> getResourceData(Long deviceId, String resourceName);

    List<DeviceTable> findAllTables();
}
