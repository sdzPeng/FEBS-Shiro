package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.constants.DeviceFailureConstants;
import cc.mrbird.febs.business.constants.FixedValueConstants;
import cc.mrbird.febs.business.dto.DeviceDataDto;
import cc.mrbird.febs.business.entity.*;
import cc.mrbird.febs.business.mapper.FixedValueMapper;
import cc.mrbird.febs.business.service.*;
import cc.mrbird.febs.business.util.NumberUtil;
import cc.mrbird.febs.common.exception.FebsException;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-22 2:51 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FixedValueServiceImpl extends ServiceImpl<FixedValueMapper, FixedValue> implements IFixedValueService {
    private final String FIXED_VALUE_REGEXP = "定值表[\\d]*";
    private final String DIRECTION = "公里标方向(相减-1/相加1)";
    @Autowired private IFixedValueTableService fixedValueTableService;
    @Autowired private IFixedValueVersionService fixedValueVersionService;
    @Autowired private IResourceService resourceService;
    @Autowired private IDeviceTableService deviceTableService;
    @Autowired private IDeviceService deviceService;
    @Autowired private IDeviceDataService deviceDataService;
    @Autowired private IDeviceResourceService deviceResourceService;
    public static ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public Long analysis(ReadSheet readSheet, List<FixedValue> list, Resource resource) {
        if (null == readSheet||!readSheet.getSheetName().matches(FIXED_VALUE_REGEXP)) throw new FebsException("sheet页命名要求「定值表标识符数字」！");
        FixedValue direction = list.stream().filter(o -> DIRECTION.equals(o.getName())).findFirst().orElseThrow(()->new FebsException("定值表中确实属性「公里标方向(相减-1/相加1)」"));
        Long fixedValueTableId = extracted(resource, readSheet.getSheetName());

        if (!ObjectUtils.isEmpty(direction)) {
            updateVersion(direction);
        }
        list.remove(direction);
        return fixedValueTableId;
    }

    private Long extracted(Resource resource, String sheetName) {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", sheetName);
        final FixedValueTable temp;
        FixedValueTable fixValueTable = fixedValueTableService.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(fixValueTable)) {
            temp = new FixedValueTable();
            temp.setName(sheetName);
            temp.setCreateTime(new Date());
            fixedValueTableService.save(temp);
        }else {
            temp = fixValueTable;
        }
        QueryWrapper<FixedValueVersion> fixedValueVersionQueryWrapper = new QueryWrapper<>();
        fixedValueVersionQueryWrapper.eq("FIXED_VALUE_TABLE_ID", temp.getFixedValueTableId());
//        FixedValueVersion fixValueVersion = fixedValueVersionService.getOne(fixedValueVersionQueryWrapper);
//        if (null != fixValueVersion) {
//            fixedValueTableService.delFixedValueVersion(new ArrayList<Long>(){{add(fixValueVersion.getFixValueVersionId());}});
//        }
        int count = fixedValueVersionService.count(fixedValueVersionQueryWrapper);
        FixedValueVersion fixedValueVersion = new FixedValueVersion();
        resourceService.save(resource);
        fixedValueVersion.setResourceId(resource.getResourceId());
        fixedValueVersion.setCreateTime(new Date());
        // todo 版本控制
        fixedValueVersion.setVersion(NumberUtil.numberToChinaStr(count));
        fixedValueVersion.setFixedValueTableId(temp.getFixedValueTableId());
        fixedValueVersionService.save(fixedValueVersion);
        THREAD_LOCAL.set(fixedValueVersion.getFixValueVersionId());
        return temp.getFixedValueTableId();
    }

    /**
     * 加上存储数据库
     */
    @Override
    public void saveData(List<FixedValue> list) {
        log.info("{}条数据，开始存储数据库！", list.size());
        list.forEach(o->o.setFixedValueVersionId(THREAD_LOCAL.get()));
        saveBatch(list);
        System.out.println(list);
        log.info("存储数据库成功！");
    }

    @Override
    public void updateVersion(FixedValue fixValue) {
        final FixedValueVersion fixedValueVersion = this.fixedValueVersionService.getById(THREAD_LOCAL.get());
        fixedValueVersion.setDirection(fixValue.getSummonValue());
        this.fixedValueVersionService.updateById(fixedValueVersion);
    }

    @Override
    public FixedValue getOneByDeviceIdAndFixedValueName(Long deviceId, String fixedValueName) {
        Device device = this.deviceService.getById(deviceId);
        if (null == device) throw new FebsException(String.format("设备唯一标识「%s」不存在", deviceId));
        DeviceTable deviceTable = this.deviceTableService.getById(device.getDeviceTableId());
        FixedValueVersion fixedValueVersion = this.fixedValueVersionService.getById(deviceTable.getFixedValueVersionId());
        QueryWrapper<FixedValue> fixedValueQueryWrapper = new QueryWrapper<>();
        fixedValueQueryWrapper.eq("FIXED_VALUE_VERSION_ID", fixedValueVersion.getFixValueVersionId());
        fixedValueQueryWrapper.eq("NAME", fixedValueName);
        return getOne(fixedValueQueryWrapper);
    }

    @Override
    public List<DeviceDataDto> findByFixedValueVersionIdAndDimension(Long deviceId,
                                                           List<DeviceFailureConstants.DIMENSION> params) {
        Device device = deviceService.getById(deviceId);
        return params.stream().map(o -> {
            QueryWrapper<DeviceResource> deviceResourceQueryWrapper = new QueryWrapper<>();
            deviceResourceQueryWrapper.eq("DEVICE_ID", device.getDeviceId());
            deviceResourceQueryWrapper.eq("DEVICE_RESOURCE_NAME", o.getResource());
            List<DeviceResource> deviceResources = deviceResourceService.list(deviceResourceQueryWrapper).stream().distinct().collect(Collectors.toList());
            return deviceResources.stream().map(deviceResource -> {
                QueryWrapper<DeviceData> dataQueryWrapper = new QueryWrapper<>();
                dataQueryWrapper.eq("DEVICE_RESOURCE_ID", deviceResource.getDeviceResourceId());
                dataQueryWrapper.in("DEVICE_KEY", o.getName());
                return deviceDataService.list(dataQueryWrapper).stream().map(deviceData -> {
                    DeviceDataDto deviceDataDto = new DeviceDataDto();
                    deviceDataDto.setDeviceResourceName(deviceResource.getDeviceResourceName());
                    deviceDataDto.setDeviceName(o.getResource());
                    deviceDataDto.setDesc(o.getDesc());
                    deviceDataDto.setDirection(o.getDirection());
                    BeanUtils.copyProperties(deviceData, deviceDataDto);
                    return deviceDataDto;
                }).collect(Collectors.toList());
            }).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        }).flatMap(Collection::stream).distinct().collect(Collectors.toList());

    }

    @Override
    public FixedValue findByDeviceIdAndFixedName(Long deviceId, FixedValueConstants.DIMENSION dimension) {
        Device device = deviceService.getById(deviceId);
        DeviceTable deviceTable = deviceTableService.getById(device.getDeviceTableId());
        FixedValueVersion fixedValueVersion = fixedValueVersionService.getById(deviceTable.getFixedValueVersionId());
        QueryWrapper<FixedValue> fixedValueQueryWrapper = new QueryWrapper<>();
        fixedValueQueryWrapper.eq("FIXED_VALUE_VERSION_ID", fixedValueVersion.getFixValueVersionId());
        fixedValueQueryWrapper.eq("name",dimension.getName());
        return getOne(fixedValueQueryWrapper);
    }
}
