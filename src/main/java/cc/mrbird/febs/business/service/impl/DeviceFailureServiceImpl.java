package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.entity.DeviceData;
import cc.mrbird.febs.business.entity.DeviceResource;
import cc.mrbird.febs.business.entity.DeviceTable;
import cc.mrbird.febs.business.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 7:48 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DeviceFailureServiceImpl implements IDeviceFailureService {

    @Autowired private IDeviceService deviceService;
    @Autowired private IDeviceDataService deviceDataService;
    @Autowired private IDeviceResourceService deviceResourceService;
    @Autowired private IDeviceTableService deviceTableService;
    @Override
    public Object getDevicesByDeviceTableId(Long deviceTableId) {
        QueryWrapper<Device> deviceQueryWrapper = new QueryWrapper<>();
        deviceQueryWrapper.eq("DEVICE_TABLE_ID", deviceTableId);
        return deviceService.list(deviceQueryWrapper);
    }

    @Override
    public List<DeviceData> getResourceData(Long deviceId, String resourceName) {
        Device device = deviceService.getById(deviceId);
        QueryWrapper<DeviceResource> deviceResourceQueryWrapper = new QueryWrapper<>();
        deviceResourceQueryWrapper.eq("DEVICE_ID", device.getDeviceId());
        deviceResourceQueryWrapper.eq("DEVICE_RESOURCE_NAME", resourceName);
        DeviceResource deviceResource = deviceResourceService.getOne(deviceResourceQueryWrapper);
        QueryWrapper<DeviceData> deviceDataQueryWrapper = new QueryWrapper<>();
        deviceDataQueryWrapper.eq("DEVICE_RESOURCE_ID", deviceResource.getDeviceResourceId());
        return deviceDataService.list(deviceDataQueryWrapper);
    }

    @Override
    public List<DeviceTable> findAllTables() {
        return deviceTableService.list();
    }
}
