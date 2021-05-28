package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.dto.DeviceDto;
import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.entity.DeviceData;
import cc.mrbird.febs.business.entity.DeviceResource;
import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.service.IDeviceDataService;
import cc.mrbird.febs.business.service.IDeviceResourceService;
import cc.mrbird.febs.business.service.IDeviceService;
import cc.mrbird.febs.business.service.IResourceService;
import cc.mrbird.febs.business.util.ApplicationContextUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final IDeviceService deviceService;
    private final IDeviceDataService deviceDataService;
    private final IDeviceResourceService deviceResourceService;
    private final IResourceService resourceService;
    List<Device> list = new ArrayList<>();
    private final ThreadLocal<Long> DEVICE_THREAD_LOCAL = new ThreadLocal<>();
    private Resource resource;

    public DeviceListener() {
        deviceService =  ApplicationContextUtil.getBean(IDeviceService.class);
        deviceDataService = ApplicationContextUtil.getBean(IDeviceDataService.class);
        deviceResourceService = ApplicationContextUtil.getBean(IDeviceResourceService.class);
        resourceService =  ApplicationContextUtil.getBean(IResourceService.class);
    }

    public DeviceListener(Long fixedValueVersionId, Resource resource) {
        DEVICE_THREAD_LOCAL.set(fixedValueVersionId);
        deviceService =  ApplicationContextUtil.getBean(IDeviceService.class);
        deviceDataService = ApplicationContextUtil.getBean(IDeviceDataService.class);
        deviceResourceService = ApplicationContextUtil.getBean(IDeviceResourceService.class);
        resourceService =  ApplicationContextUtil.getBean(IResourceService.class);
        this.resource = resource;
    }

    @Override
    public void invoke(DeviceDto device, AnalysisContext analysisContext) {
        Device target = new Device();
        QueryWrapper<Resource> resourceQueryWrapper = new QueryWrapper<>();
        resourceQueryWrapper.eq("uuid", resource.getUuid());
        int count = resourceService.count(resourceQueryWrapper);
        if (NumberUtils.compare(0, count) == 0) {
            resourceService.save(resource);
        }
        target.setResourceId(resource.getResourceId());
        BeanUtils.copyProperties(device, target);
        list.add(target);
    }

    private void saveData() {
        log.info("{}条数据，开始存储数据库！", list.size());
        list.forEach(o->o.setFixedValueVersionId(DEVICE_THREAD_LOCAL.get()));
        Map<String, List<Device>> deviceGroups = list.stream().collect(Collectors.groupingBy(Device::getDeviceName));
        deviceGroups.forEach((k, v) -> {
            // 保存第一个设备，设备源保存v.size() 条数
            Device device = v.get(0);
            deviceService.save(device);
            // 保存每个设备的数据源
            v.forEach(o-> {
                String[] split = o.getContent().split("\\n");
                // device_resource_name
                DeviceResource deviceResource = new DeviceResource();
                deviceResource.setDeviceResourceName(split[0]);
                deviceResource.setDeviceId(device.getDeviceId());
                deviceResourceService.save(deviceResource);
                // others
                List<DeviceData> deviceDatas = new ArrayList<>();
                for (int i = 1; i < split.length; i++) {
                    String[] keyValue = split[i].split("=");
                    if (keyValue.length != 2) continue;
                    DeviceData deviceData = new DeviceData();
                    deviceData.setDeviceKey(keyValue[0]);
                    deviceData.setDeviceValue(keyValue[1]);
                    deviceData.setDeviceResourceId(deviceResource.getDeviceResourceId());
                    deviceDatas.add(deviceData);
                }
                deviceDataService.saveBatch(deviceDatas);
            });
            
        });
        System.out.println(list);
        log.info("存储数据库成功！");
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        list.clear();
        DEVICE_THREAD_LOCAL.remove();
    }
}
