package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.FixedValueTable;
import cc.mrbird.febs.business.entity.FixedValueVersion;
import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.mapper.FixedValueMapper;
import cc.mrbird.febs.business.service.IFixedValueService;
import cc.mrbird.febs.business.service.IFixedValueTableService;
import cc.mrbird.febs.business.service.IFixedValueVersionService;
import cc.mrbird.febs.business.service.IResourceService;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
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
    public static ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public void analysis(ReadSheet readSheet, List<FixedValue> list, Resource resource) {
        if (null == readSheet||!readSheet.getSheetName().matches(FIXED_VALUE_REGEXP)) return;
        FixedValue direction = list.stream().filter(o -> DIRECTION.equals(o.getName())).findFirst().get();
        extracted(resource, readSheet.getSheetName());
        if (!ObjectUtils.isEmpty(direction)) {
            updateVersion(direction);
        }
        list.remove(direction);
    }

    private void extracted(Resource resource, String sheetName) {
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
        FixedValueVersion fixedValueVersion = new FixedValueVersion();
        resourceService.save(resource);
        fixedValueVersion.setResourceId(resource.getResourceId());
        fixedValueVersion.setCreateTime(new Date());
        // todo 版本控制
        fixedValueVersion.setVersion("一");
        fixedValueVersion.setFixedValueTableId(temp.getFixedValueTableId());
        fixedValueVersionService.save(fixedValueVersion);
        THREAD_LOCAL.set(fixedValueVersion.getFixValueVersionId());
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
}
