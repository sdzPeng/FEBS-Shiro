package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.entity.Device;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-25 7:04 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
public interface IDeviceService extends IService<Device> {

    void saveData(List<Device> list);
}
