package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.service.IDeviceFailureService;
import cc.mrbird.febs.business.service.IDeviceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Override
    public Object getDevicesByDeviceTableId(String deviceTableId) {
        QueryWrapper<Device> deviceQueryWrapper = new QueryWrapper<>();
        deviceQueryWrapper.eq("DEVICE_TABLE_ID", deviceTableId);
        return deviceService.list(deviceQueryWrapper);
    }
}
