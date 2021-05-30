package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.dto.FixedValueTableDto;
import cc.mrbird.febs.business.entity.FixedValueTable;
import cc.mrbird.febs.business.entity.FixedValueVersion;
import cc.mrbird.febs.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-24 5:57 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public interface IFixedValueTableService  extends IService<FixedValueTable> {
    IPage<?> fixedValueTableList(QueryRequest request);

    List<FixedValueTableDto> fixedValueTableList();

    void delValueTable(Long fixedValueTableId);

    void delAllValueTable();

    void delFixedValueVersion(List<Long> fixedValueTableIds);
}
