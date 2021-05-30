package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.dto.FixedValueTableDto;
import cc.mrbird.febs.business.entity.*;
import cc.mrbird.febs.business.mapper.FixedValueTableMapper;
import cc.mrbird.febs.business.service.*;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.core.util.CollectionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-24 5:57 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FixedValueTableServiceImpl extends ServiceImpl<FixedValueTableMapper, FixedValueTable> implements IFixedValueTableService {

    @Autowired private IFixedValueVersionService fixedValueVersionService;
    @Autowired private IFixedValueMetaService fixedValueMetaService;
    @Autowired private IFixedValueService fixedValueService;
    @Autowired private IFixedValueTableService fixedValueTableService;
    @Autowired private IDeviceTableService deviceTableService;
    @Autowired private IDeviceService deviceService;
    @Autowired private IDeviceResourceService deviceResourceService;
    @Autowired private IDeviceDataService deviceDataService;
    @Autowired private IResourceService resourceService;
    @Autowired private GridFsTemplate gridFsTemplate;
    @Override
    public IPage<?> fixedValueTableList(QueryRequest request) {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        Page<FixedValueTable> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, true);
        return this.page(page, queryWrapper);
    }

    @Override
    public List<FixedValueTableDto> fixedValueTableList() {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("CREATE_TIME");

        return fixedValueTableService.list(queryWrapper).stream().map(o-> {
            FixedValueTableDto fixedValueTableDto = new FixedValueTableDto();
            BeanUtils.copyProperties(o, fixedValueTableDto);
            QueryWrapper<FixedValueVersion> fixedValueVersionQueryWrapper = new QueryWrapper<>();
            fixedValueVersionQueryWrapper.eq("FIXED_VALUE_TABLE_ID", o.getFixedValueTableId());
            List<Long> fixValueVersions = fixedValueVersionService.list(fixedValueVersionQueryWrapper).stream().map(FixedValueVersion::getFixValueVersionId).collect(Collectors.toList());
            fixedValueTableDto.setFixedValueVersionId(fixValueVersions);
            return fixedValueTableDto;
        }).collect(Collectors.toList());
    }

    @Override
    public void delValueTable(Long fixedValueTableId) {
        QueryWrapper<FixedValueVersion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FIXED_VALUE_TABLE_ID", fixedValueTableId);
        List<FixedValueVersion> fixedValueVersions = fixedValueVersionService.list(queryWrapper);
        delFixedValueVersion(fixedValueVersions.stream().map(FixedValueVersion::getFixValueVersionId).collect(Collectors.toList()));
        QueryWrapper<FixedValueTable> tableQueryWrapper = new QueryWrapper<>();
        tableQueryWrapper.eq("FIXED_VALUE_TABLE_ID", fixedValueTableId);
        fixedValueTableService.remove(tableQueryWrapper);
    }

    @Override
    public void delFixedValueVersion(List<Long> fixValueVersionId) {
        QueryWrapper<FixedValueVersion> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("FIXED_VALUE_VERSION_ID", fixValueVersionId);
        List<FixedValueVersion> fixedValueVersions = fixedValueVersionService.list(queryWrapper);
        List<Long> resourceIds = fixedValueVersions.stream().map(FixedValueVersion::getResourceId).distinct().collect(Collectors.toList());
        fixedValueVersions.forEach(o-> {
            QueryWrapper<FixedValue> fixedValueQueryWrapper = new QueryWrapper<>();
            fixedValueQueryWrapper.eq("FIXED_VALUE_VERSION_ID", o.getFixValueVersionId());
            fixedValueService.remove(fixedValueQueryWrapper);
            QueryWrapper<FixedValueMeta> tableMetaQueryWrapper = new QueryWrapper<>();
            tableMetaQueryWrapper.eq("FIXED_VALUE_VERSION_ID", o.getFixValueVersionId());
            fixedValueMetaService.remove(tableMetaQueryWrapper);
        });
        if (CollectionUtils.isEmpty(fixedValueVersions)) return;
        // 删除关联的设备故障数据
        QueryWrapper<DeviceTable> deviceTableQueryWrapper = new QueryWrapper<>();
        deviceTableQueryWrapper.in("FIXED_VALUE_VERSION_ID", fixedValueVersions.stream().map(FixedValueVersion::getFixValueVersionId).collect(Collectors.toList()));
        List<DeviceTable> deviceTalbeList = deviceTableService.list(deviceTableQueryWrapper);
        // 删除关联文件
        resourceIds.addAll(deviceTalbeList.stream().map(DeviceTable::getResourceId).distinct().collect(Collectors.toList()));
        List<String> uuids = resourceService.listByIds(resourceIds).stream().map(Resource::getUuid).collect(Collectors.toList());
        Query query = Query.query(GridFsCriteria.where("metadata.uuid").in(uuids));
        gridFsTemplate.delete(query);
        log.info("文件[{}]删除完成！", uuids.toString());
        QueryWrapper<DeviceResource> deviceResourceQueryWrapper = new QueryWrapper<>();
        List<Long> deviceTableIds = deviceTalbeList.stream().map(DeviceTable::getDeviceTableId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(deviceTableIds)) {
            QueryWrapper<Device> deviceQueryWrapper = new QueryWrapper<>();
            deviceQueryWrapper.in("DEVICE_TABLE_ID", deviceTableIds);
            List<Device> list = deviceService.list(deviceQueryWrapper);
            List<Long> deviceIds = list.stream().map(Device::getDeviceId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(deviceIds)) {
                deviceResourceQueryWrapper.in("DEVICE_ID",deviceIds);
                List<Long> deviceResourceIds = deviceResourceService.list(deviceResourceQueryWrapper).stream().map(DeviceResource::getDeviceResourceId).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(deviceResourceIds)) {
                    QueryWrapper<DeviceData> deviceDataQueryWrapper = new QueryWrapper<>();
                    deviceDataQueryWrapper.in("DEVICE_RESOURCE_ID", deviceResourceIds);
                    deviceDataService.remove(deviceDataQueryWrapper);
                }
                deviceResourceService.remove(deviceResourceQueryWrapper);
                deviceService.remove(deviceQueryWrapper);
            }
            deviceTableService.remove(deviceTableQueryWrapper);
        }
        fixedValueVersionService.remove(queryWrapper);
    }

    @Override
    public void delAllValueTable() {
        fixedValueTableService.list().forEach(o->delValueTable(o.getFixedValueTableId()));
    }
}
