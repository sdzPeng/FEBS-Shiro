package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.service.IResourceService;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-28 12:05 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("resource")
public class ResourceController extends BaseController {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private IResourceService resourceService;

    @GetMapping("download")
    public void download(Long resourceId, HttpServletResponse response) throws IOException {
        Resource origin = resourceService.getById(resourceId);
        Query query = Query.query(GridFsCriteria.where("metadata.uuid").is(origin.getUuid()));
        GridFSFile one = gridFsTemplate.findOne(query);
        GridFsResource resource = gridFsTemplate.getResource(one);
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = response.getOutputStream();) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            // chrome浏览器下载文件可能出现：ERR_RESPONSE_HEADERS_MULTIPLE_CONTENT_DISPOSITION，
            // 产生原因：可能是因为文件名中带有英文半角逗号,
            // 解决办法：确保 filename 参数使用双引号包裹[1]
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(origin.getFileName(), "UTF-8"));
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Expires", "0");
            byte[] arr = new byte[10];
            int len;
            while (( len=inputStream.read(arr) ) != -1 ) {
                outputStream.write(arr, 0, len);
            }
        }
    }

    @DeleteMapping("delete")
    public FebsResponse delete(Long resourceId) throws IOException {
        Resource resource = resourceService.getById(resourceId);
        Query query = Query.query(GridFsCriteria.where("metadata.uuid").is(resource.getUuid()));
        gridFsTemplate.delete(query);
        return new FebsResponse().success();
    }

}
