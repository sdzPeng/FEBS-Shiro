package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.FixedValueTable;
import cc.mrbird.febs.business.mapper.FixedValueTableMapper;
import cc.mrbird.febs.business.service.IFixedValueTableService;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Override
    public IPage<?> fixedValueTableList(QueryRequest request) {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        Page<FixedValueTable> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, true);
        return this.page(page, queryWrapper);
    }
}
