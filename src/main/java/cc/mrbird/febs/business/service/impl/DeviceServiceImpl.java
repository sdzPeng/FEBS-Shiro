package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.entity.DeviceData;
import cc.mrbird.febs.business.entity.DeviceResource;
import cc.mrbird.febs.business.mapper.DeviceMapper;
import cc.mrbird.febs.business.service.IDeviceDataService;
import cc.mrbird.febs.business.service.IDeviceResourceService;
import cc.mrbird.febs.business.service.IDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-25 7:05 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DeviceServiceImpl  extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    @Autowired private IDeviceService deviceService;
    @Autowired private IDeviceDataService deviceDataService;
    @Autowired private IDeviceResourceService deviceResourceService;
    private static final String DEVICE_REGEXP = "[\\d]*号故障测距装置";
    Pattern pattern = Pattern.compile("([\\d\\.]*)(.*)");


    @Override
    public void saveData(List<Device> list, Long fixedValueVersionId) {
        log.info("{}条数据，开始存储数据库！", list.size());
        Map<String, List<Device>> deviceGroups = list
                .stream()
                .filter(o-> o.getDeviceName().matches(DEVICE_REGEXP))
                .collect(Collectors.groupingBy(device->device.getDeviceName()+"_"+device.getFailureTime()));
        deviceGroups.forEach((k, v) -> {
            // 保存第一个设备，设备源保存v.size() 条数
            Device target = new Device();
            BeanUtils.copyProperties(v.get(0), target);
            target.setContent(v.stream().map(Device::getContent).collect(Collectors.joining("\n")));
            deviceService.save(target);
            // 保存每个设备的数据源
            v.forEach(o-> {
                String[] split = o.getContent().split("\\n");
                // device_resource_name
                DeviceResource deviceResource = new DeviceResource();
                deviceResource.setDeviceResourceName(split[0]);
                deviceResource.setDeviceId(target.getDeviceId());
                deviceResourceService.save(deviceResource);
                // others
                List<DeviceData> deviceDatas = new ArrayList<>();
                for (int i = 1; i < split.length; i++) {
                    String[] keyValue = split[i].split("=");
                    if (keyValue.length != 2) continue;
                    DeviceData deviceData = new DeviceData();
                    deviceData.setDeviceKey(keyValue[0]);
                    Matcher matcher = pattern.matcher(keyValue[1]);
                    if (matcher.find()) {
                        deviceData.setDeviceValue(matcher.group(1));
                        deviceData.setUnit(matcher.group(2));
                    }else {
                        deviceData.setDeviceValue(keyValue[1]);
                    }
                    deviceData.setDeviceResourceId(deviceResource.getDeviceResourceId());
                    deviceDatas.add(deviceData);
                }
                deviceDataService.saveBatch(deviceDatas);
            });
        });
        System.out.println(list);
        log.info("存储数据库成功！");
    }
}
