package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.DeviceResource;
import cc.mrbird.febs.business.mapper.DeviceResourceMapper;
import cc.mrbird.febs.business.service.IDeviceResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-26 1:05 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DeviceResourceServiceImpl extends ServiceImpl<DeviceResourceMapper, DeviceResource> implements IDeviceResourceService {
}
