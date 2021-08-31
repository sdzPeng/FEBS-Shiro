package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.dto.FixedTableVersionDto;
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
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-24 10:16 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
public class FixValueListener extends AnalysisEventListener<FixedValue> {

    private final IFixedValueService fixedValueService;

    private Resource resource;

    private Map<String, Object> callBack;

    public FixValueListener() {
        this.fixedValueService = ApplicationContextUtil.getBean(IFixedValueService.class);
    }

    public FixValueListener(Resource resource, Map<String, Object> callBack) {
        this.fixedValueService = ApplicationContextUtil.getBean(IFixedValueService.class);
        this.resource = resource;
        this.callBack = callBack;

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
        FixedTableVersionDto fixedTable = fixedValueService.analysis(readSheet, list,
                resource, Long.valueOf(callBack.get("siteId").toString()));
        callBack.put("fixedValueTableId", fixedTable.getFixedValueTableId());
        callBack.put("fixedValueVersionId", fixedTable.getFixedValueVersionId());
        fixedValueService.saveData(list, fixedTable.getFixedValueVersionId());
        log.info("所有数据解析完成！");
        list.clear();
    }

}
