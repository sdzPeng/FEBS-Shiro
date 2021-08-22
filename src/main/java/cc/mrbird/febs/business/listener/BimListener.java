package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.dto.BimDto;
import cc.mrbird.febs.business.entity.Bim;
import cc.mrbird.febs.business.entity.FixedValueMeta;
import cc.mrbird.febs.business.service.IBimService;
import cc.mrbird.febs.business.service.IFixedValueMetaService;
import cc.mrbird.febs.business.util.ApplicationContextUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cc.mrbird.febs.business.service.impl.FixedValueServiceImpl.THREAD_LOCAL;

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
public class BimListener extends AnalysisEventListener<BimDto> {

    final private IBimService bimService;

    public BimListener() {
        // 这里是demo，所以随便new一个。实际使用如果到了spring,请使用下面的有参构造函数
        bimService = ApplicationContextUtil.getBean(IBimService.class);
    }

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    List<BimDto> list = new ArrayList<>();

    @Override
    public void invoke(BimDto bim, AnalysisContext analysisContext) {
        log.info("解析到一条数据:{}", JSON.toJSONString(bim));
        list.add(bim);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库！", list.size());
        List<Bim> bimList = list.stream().map(o -> {
            Bim bim = new Bim();
            BeanUtils.copyProperties(o, bim);
            return bim;
        }).collect(Collectors.toList());
        bimService.saveBatch(bimList);
        System.out.println(bimList);
        log.info("存储数据库成功！");
        list.clear();
    }
}
