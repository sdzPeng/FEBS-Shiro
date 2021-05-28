package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.FixedValueTable;
import cc.mrbird.febs.business.entity.FixedValueVersion;
import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.service.IFixedValueService;
import cc.mrbird.febs.business.service.IFixedValueTableService;
import cc.mrbird.febs.business.service.IFixedValueVersionService;
import cc.mrbird.febs.business.service.IResourceService;
import cc.mrbird.febs.business.util.ApplicationContextUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import static cc.mrbird.febs.business.listener.FixValueMetaListener.FIXED_VALUE_VERSION;
import static cc.mrbird.febs.business.listener.FixValueMetaListener.THREAD_LOCAL;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-24 10:16 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
public class FixValueListener extends AnalysisEventListener<FixedValue> {

    private final IFixedValueService fixedValueService;

    final private IFixedValueTableService fixedValueTableService;

    final private IFixedValueVersionService fixedValueVersionService;

    private final IResourceService resourceService;

    public static String SHEET_NAME = "sheetName";

    private Resource resource;

    public FixValueListener() {
        this.fixedValueService = ApplicationContextUtil.getBean(IFixedValueService.class);
        this.fixedValueTableService = ApplicationContextUtil.getBean(IFixedValueTableService.class);
        this.fixedValueVersionService = ApplicationContextUtil.getBean(IFixedValueVersionService.class);
        this.resourceService = ApplicationContextUtil.getBean(IResourceService.class);
    }

    public FixValueListener(Resource resource) {
        this.fixedValueService = ApplicationContextUtil.getBean(IFixedValueService.class);
        this.fixedValueTableService = ApplicationContextUtil.getBean(IFixedValueTableService.class);
        this.fixedValueVersionService = ApplicationContextUtil.getBean(IFixedValueVersionService.class);
        this.resourceService = ApplicationContextUtil.getBean(IResourceService.class);
        this.resource = resource;
    }

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;
    List<FixedValue> list = new ArrayList<>();

    private final String FIXED_VALUE_REGEXP = "定值表[\\d]*";

    private final String DIRECTION = "公里标方向(相减-1/相加1)";

    @Override
    public void invoke(FixedValue fixValue, AnalysisContext analysisContext) {
        ReadSheet readSheet = analysisContext.readSheetHolder().getReadSheet();
        if (null == readSheet||!readSheet.getSheetName().matches(FIXED_VALUE_REGEXP)) return;
        if (ObjectUtils.isEmpty(THREAD_LOCAL.get())) {
            Map<String, Object> map = new HashMap<>(16);
            map.put(SHEET_NAME, analysisContext.readSheetHolder().getSheetName());
            THREAD_LOCAL.set(map);
            extracted();
        }
        log.info("解析到一条数据:{}", JSON.toJSONString(fixValue));
        if (StringUtils.equals(fixValue.getName(), DIRECTION)) {
            final FixedValueVersion fixedValueVersion = this.fixedValueVersionService.getById(Long.parseLong(THREAD_LOCAL.get().get(FIXED_VALUE_VERSION).toString()));
            fixedValueVersion.setDirection(fixValue.getSummonValue());
            this.fixedValueVersionService.updateById(fixedValueVersion);
        }else {
            list.add(fixValue);
            // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
            if (list.size() >= BATCH_COUNT) {
                saveData();
                // 存储完成清理 list
                list.clear();
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }

    private void extracted() {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", THREAD_LOCAL.get().get(SHEET_NAME));
        final FixedValueTable temp;
        FixedValueTable fixValueTable = fixedValueTableService.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(fixValueTable)) {
            temp = new FixedValueTable();
            temp.setName(THREAD_LOCAL.get().get(SHEET_NAME).toString());
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
        THREAD_LOCAL.get().put(FIXED_VALUE_VERSION, fixedValueVersion.getFixValueVersionId());
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库！", list.size());
        list.forEach(o->o.setFixedValueVersionId(Long.parseLong(THREAD_LOCAL.get().get(FIXED_VALUE_VERSION).toString())));
        fixedValueService.saveBatch(list);
        System.out.println(list);
        log.info("存储数据库成功！");
    }
}
