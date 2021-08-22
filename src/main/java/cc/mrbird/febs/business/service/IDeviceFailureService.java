package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.entity.DeviceData;
import cc.mrbird.febs.business.entity.DeviceTable;

import java.util.List;
import java.util.Map;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-30 7:47 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
public interface IDeviceFailureService {
    Object getDevicesByDeviceTableId(Long deviceTableId);

    List<DeviceData> getResourceData(Long deviceId, String resourceName);

    List<DeviceTable> findAllTables();

    void attachFixedTableVersion(Long deviceTableId, Long fixedValueVersionId);
}
