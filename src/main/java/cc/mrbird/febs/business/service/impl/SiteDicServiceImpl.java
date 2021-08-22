package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.entity.SiteDic;
import cc.mrbird.febs.business.mapper.SiteDicMapper;
import cc.mrbird.febs.business.service.ISiteDicService;
import cc.mrbird.febs.common.authentication.ShiroRealm;
import cc.mrbird.febs.common.entity.MenuTree;
import cc.mrbird.febs.common.utils.TreeUtil;
import cc.mrbird.febs.system.entity.Menu;
import cc.mrbird.febs.system.service.IRoleMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-07-25 1:35 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SiteDicServiceImpl extends ServiceImpl<SiteDicMapper, SiteDic> implements ISiteDicService {
    @Autowired
    private IRoleMenuService roleMenuService;
    @Autowired
    private ShiroRealm shiroRealm;
    @Override
    public MenuTree<SiteDic> findSites(SiteDic siteDic) {
        QueryWrapper<SiteDic> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(siteDic.getSiteName())) {
            queryWrapper.lambda().like(SiteDic::getSiteName, siteDic.getSiteName());
        }
        queryWrapper.lambda().orderByAsc(SiteDic::getOrderNum);
        List<SiteDic> menus = this.baseMapper.selectList(queryWrapper);
        List<MenuTree<SiteDic>> trees = this.convertMenus(menus);
        return TreeUtil.buildMenuTree(trees);
    }

    @Override
    public void createSite(SiteDic siteDic) {
        Date date = new Date();
        siteDic.setCreateTime(date);
        siteDic.setModifyTime(date);
        this.setSite(siteDic);
        this.baseMapper.insert(siteDic);
    }

    @Override
    public void deleteSites(String siteIds) {
        String[] menuIdsArray = siteIds.split(StringPool.COMMA);
        delete(Arrays.asList(menuIdsArray));
        shiroRealm.clearCache();
    }

    @Override
    public void updateSite(SiteDic siteDic) {
        siteDic.setModifyTime(new Date());
        this.setSite(siteDic);
        this.baseMapper.updateById(siteDic);

        shiroRealm.clearCache();
    }

    private void delete(List<String> siteIds) {
        List<String> list = new ArrayList<>(siteIds);
        removeByIds(siteIds);

        LambdaQueryWrapper<SiteDic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SiteDic::getParentId, siteIds);
        List<SiteDic> menus = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(menus)) {
            List<String> menuIdList = new ArrayList<>();
            menus.forEach(m -> menuIdList.add(String.valueOf(m.getSiteId())));
            list.addAll(menuIdList);
            this.roleMenuService.deleteRoleMenusByMenuId(list);
            this.delete(menuIdList);
        } else {
            this.roleMenuService.deleteRoleMenusByMenuId(list);
        }
    }

    private void setSite(SiteDic site) {
        if (site.getParentId() == null)
            site.setParentId(Menu.TOP_NODE);
    }

    private List<MenuTree<SiteDic>> convertMenus(List<SiteDic> sites) {
        List<MenuTree<SiteDic>> trees = new ArrayList<>();
        sites.forEach(siteDic -> {
            MenuTree<SiteDic> tree = new MenuTree<>();
            tree.setId(String.valueOf(siteDic.getSiteId()));
            tree.setParentId(String.valueOf(siteDic.getParentId()));
            tree.setTitle(siteDic.getSiteName());
            tree.setData(siteDic);
            trees.add(tree);
        });
        return trees;
    }

}
