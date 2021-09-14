package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.*;
import cc.mrbird.febs.business.mapper.DeviceTableMapper;
import cc.mrbird.febs.business.service.*;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-30 7:48 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DeviceFailureServiceImpl extends ServiceImpl<DeviceTableMapper, DeviceTable> implements IDeviceFailureService {

    @Autowired private IDeviceService deviceService;
    @Autowired private IDeviceDataService deviceDataService;
    @Autowired private IDeviceResourceService deviceResourceService;
    @Autowired private IDeviceTableService deviceTableService;
    @Autowired private IFixedValueVersionService versionService;
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

    @Override
    public void attachFixedTableVersion(Long deviceTableId, Long fixedValueVersionId) {
        FixedValueVersion fixedValueVersion = versionService.getById(fixedValueVersionId);
        if (null == fixedValueVersion) throw new FebsException(String.format("定制表版本id「%s」不存在", fixedValueVersionId));
        QueryWrapper<DeviceTable> deviceTableQueryWrapper = new QueryWrapper<>();
        deviceTableQueryWrapper.eq("DEVICE_TABLE_ID", deviceTableId);
        DeviceTable deviceTable = deviceTableService.getOne(deviceTableQueryWrapper);
        if (null == deviceTable) throw new FebsException(String.format("设备表表id「%s」不存在", deviceTableId));
        deviceTable.setFixedValueVersionId(fixedValueVersionId);
        deviceTableService.update(deviceTable, deviceTableQueryWrapper);
    }

    @Override
    public IPage<DeviceTable> findTableByPage(QueryRequest request, Integer fixedValueVersionId) {
        QueryWrapper<DeviceTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FIXED_VALUE_VERSION_ID", fixedValueVersionId);
        Page<DeviceTable> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, true);
        return this.page(page, queryWrapper);
    }
}
