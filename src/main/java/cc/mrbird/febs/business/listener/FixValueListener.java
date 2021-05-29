package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.service.IFixedValueService;
import cc.mrbird.febs.business.util.ApplicationContextUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

    private Resource resource;

    public FixValueListener() {
        this.fixedValueService = ApplicationContextUtil.getBean(IFixedValueService.class);
    }

    public FixValueListener(Resource resource) {
        this.fixedValueService = ApplicationContextUtil.getBean(IFixedValueService.class);
        this.resource = resource;
    }

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    List<FixedValue> list = new ArrayList<>();

    @Override
    public void invoke(FixedValue fixValue, AnalysisContext analysisContext) {
        log.info("解析到一条数据:{}", JSON.toJSONString(fixValue));
        list.add(fixValue);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        ReadSheet readSheet = analysisContext.readSheetHolder().getReadSheet();
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        fixedValueService.analysis(readSheet, list, resource);
        fixedValueService.saveData(list);
        log.info("所有数据解析完成！");
        list.clear();
    }

}
