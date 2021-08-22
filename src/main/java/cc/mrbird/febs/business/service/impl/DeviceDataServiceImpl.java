package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.DeviceData;
import cc.mrbird.febs.business.mapper.DeviceDataMapper;
import cc.mrbird.febs.business.service.IDeviceDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-25 7:05 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DeviceDataServiceImpl extends ServiceImpl<DeviceDataMapper, DeviceData> implements IDeviceDataService {
}
