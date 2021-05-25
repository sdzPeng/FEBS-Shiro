package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.entity.FixedValueVersion;
import cc.mrbird.febs.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-25 12:20 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public interface IFixedValueVersionService extends IService<FixedValueVersion> {
    IPage<?> fixedValueVersionList(String fixedValueTableId, QueryRequest request);
}
