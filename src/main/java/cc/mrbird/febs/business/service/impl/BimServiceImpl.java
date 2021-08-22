package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.Bim;
import cc.mrbird.febs.business.mapper.BimMapper;
import cc.mrbird.febs.business.service.IBimService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-08-21 11:36 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class BimServiceImpl extends ServiceImpl<BimMapper, Bim> implements IBimService {

}
