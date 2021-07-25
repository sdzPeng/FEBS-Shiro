package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.entity.SiteDic;
import cc.mrbird.febs.business.service.ISiteDicService;
import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.MenuTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Collections;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-07-25 1:44 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("site")
public class SiteDicController extends BaseController {

    @Autowired
    private ISiteDicService siteDicService;

    @PostMapping
    @RequiresPermissions("site:add")
    @ControllerEndpoint(operation = "新增站点", exceptionMessage = "新增站点失败")
    public FebsResponse addMenu(@Valid SiteDic siteDic) {
        this.siteDicService.createSite(siteDic);
        return new FebsResponse().success();
    }

    @GetMapping("delete/{siteIds}")
    @RequiresPermissions("site:delete")
    @ControllerEndpoint(operation = "删除站点", exceptionMessage = "删除站点失败")
    public FebsResponse deleteSites(@NotBlank(message = "{required}") @PathVariable String siteIds) {
        this.siteDicService.deleteSites(siteIds);
        return new FebsResponse().success();
    }

    @GetMapping("tree")
    @ControllerEndpoint(exceptionMessage = "获取站点树失败")
    public FebsResponse getSiteTree(SiteDic siteDic) {
        MenuTree<SiteDic> menus = this.siteDicService.findSites(siteDic);
        return new FebsResponse().success().data(menus.getChilds());
    }

    @PostMapping("update")
    @RequiresPermissions("site:update")
    @ControllerEndpoint(operation = "修改站点", exceptionMessage = "修改站点失败")
    public FebsResponse updateSite(@Valid SiteDic siteDic) {
        this.siteDicService.updateSite(siteDic);
        return new FebsResponse().success();
    }
}
