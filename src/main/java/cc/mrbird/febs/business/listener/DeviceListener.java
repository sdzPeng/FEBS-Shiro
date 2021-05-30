package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.dto.DeviceDto;
import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.entity.DeviceTable;
import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.service.IDeviceService;
import cc.mrbird.febs.business.service.IDeviceTableService;
import cc.mrbird.febs.business.service.IResourceService;
import cc.mrbird.febs.business.util.ApplicationContextUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-25 8:27 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
public class DeviceListener extends AnalysisEventListener<DeviceDto> {
    private IDeviceService deviceService;
    private final IResourceService resourceService;
    private final IDeviceTableService deviceTableService;
    List<Device> list = new ArrayList<>();
    private Long fixedValueVersionId;
    private Resource resource;
    private String fileName;
    Map<String, Object> callBack;

    public DeviceListener() {
        this.resourceService =  ApplicationContextUtil.getBean(IResourceService.class);
        this.deviceService = ApplicationContextUtil.getBean(IDeviceService.class);
        this.deviceTableService = ApplicationContextUtil.getBean(IDeviceTableService.class);
    }

    public DeviceListener(Long fixedValueVersionId, Resource resource, String fileName, Map<String, Object> callBack) {
        this.fixedValueVersionId = fixedValueVersionId;
        this.resourceService =  ApplicationContextUtil.getBean(IResourceService.class);
        this.deviceService = ApplicationContextUtil.getBean(IDeviceService.class);
        this.deviceTableService = ApplicationContextUtil.getBean(IDeviceTableService.class);
        this.resource = resource;
        this.fileName = fileName;
        this.callBack = callBack;
    }

    @Override
    public void invoke(DeviceDto device, AnalysisContext analysisContext) {
        QueryWrapper<Resource> resourceQueryWrapper = new QueryWrapper<>();
        resourceQueryWrapper.eq("uuid", resource.getUuid());
        int count = resourceService.count(resourceQueryWrapper);
        DeviceTable deviceTable;
        if (NumberUtils.compare(0, count) == 0) {
            resourceService.save(resource);
            deviceTable = new DeviceTable();
            deviceTable.setResourceId(resource.getResourceId());
            deviceTable.setName(fileName);
            deviceTable.setCreateTime(new Date());
            deviceTable.setFixedValueVersionId(fixedValueVersionId);
            deviceTableService.save(deviceTable);
        }else {
            QueryWrapper<DeviceTable> deviceTableWrapper = new QueryWrapper<>();
            deviceTableWrapper.eq("RESOURCE_ID", resource.getResourceId());
            deviceTable = deviceTableService.getOne(deviceTableWrapper);
        }
        callBack.put("deviceTableId", deviceTable.getDeviceTableId());
        Device target = new Device();
        target.setDeviceTableId(deviceTable.getDeviceTableId());
        BeanUtils.copyProperties(device, target);
        target.setDeviceId(deviceTable.getDeviceTableId());
        list.add(target);
    }



    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        deviceService.saveData(list, fixedValueVersionId);
        list.clear();
    }
}
