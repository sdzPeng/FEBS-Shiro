package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.entity.SiteDic;
import cc.mrbird.febs.business.mapper.SiteDicMapper;
import cc.mrbird.febs.common.entity.MenuTree;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-07-25 1:34 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
public interface ISiteDicService  extends IService<SiteDic> {
    MenuTree<SiteDic> findSites(SiteDic siteDic);

    void createSite(SiteDic siteDic);

    void deleteSites(String siteIds);

    void updateSite(SiteDic siteDic);
}
