package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.entity.FixedValueMeta;
import cc.mrbird.febs.business.service.IFixedValueMetaService;
import cc.mrbird.febs.business.util.ApplicationContextUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-24 11:00 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FixValueMetaListener extends AnalysisEventListener<FixedValueMeta> {

    final private IFixedValueMetaService fixedValueMetaService;

    public static String FIXED_VALUE_VERSION = "fixedValueVersion";

    public static ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public FixValueMetaListener() {
        // 这里是demo，所以随便new一个。实际使用如果到了spring,请使用下面的有参构造函数
        fixedValueMetaService = ApplicationContextUtil.getBean(IFixedValueMetaService.class);
    }

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;
    List<FixedValueMeta> list = new ArrayList<>();

    @Override
    public void invoke(FixedValueMeta fixValue, AnalysisContext analysisContext) {
        log.info("解析到一条数据:{}", JSON.toJSONString(fixValue));
        list.add(fixValue);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
        THREAD_LOCAL.remove();
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        list.forEach(o->o.setFixedValueVersionId(Long.parseLong(THREAD_LOCAL.get().get(FIXED_VALUE_VERSION).toString())));
        log.info("{}条数据，开始存储数据库！", list.size());
        fixedValueMetaService.saveBatch(list);
        System.out.println(list);
        log.info("存储数据库成功！");
    }
}
