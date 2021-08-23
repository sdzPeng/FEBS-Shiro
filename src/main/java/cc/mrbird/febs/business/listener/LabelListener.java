package cc.mrbird.febs.business.listener;

import cc.mrbird.febs.business.dto.BimDto;
import cc.mrbird.febs.business.dto.LabelDto;
import cc.mrbird.febs.business.entity.Bim;
import cc.mrbird.febs.business.entity.Label;
import cc.mrbird.febs.business.service.IBimService;
import cc.mrbird.febs.business.service.ILabelService;
import cc.mrbird.febs.business.util.ApplicationContextUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-24 11:00 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LabelListener extends AnalysisEventListener<LabelDto> {

    final private ILabelService bimService;

    public LabelListener() {
        // 这里是demo，所以随便new一个。实际使用如果到了spring,请使用下面的有参构造函数
        bimService = ApplicationContextUtil.getBean(ILabelService.class);
    }

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    List<LabelDto> list = new ArrayList<>();

    @Override
    public void invoke(LabelDto bim, AnalysisContext analysisContext) {
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
        List<Label> labelList = list.stream().map(o -> {
            Label label = new Label();
            BeanUtils.copyProperties(o, label);
            return label;
        }).collect(Collectors.toList());
        bimService.saveBatch(labelList);
        System.out.println(labelList);
        log.info("存储数据库成功！");
        list.clear();
    }
}
