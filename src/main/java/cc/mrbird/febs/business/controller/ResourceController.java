package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.service.IResourceService;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.ValidaException;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import com.spire.xls.*;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-28 12:05 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("resource")
@Api(tags = "资源操作服务")
public class ResourceController extends BaseController {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private IResourceService resourceService;

    @GetMapping("download")
    @ApiOperation(value = "资源下载")
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
    @ApiOperation(value = "删除")
    public FebsResponse delete(Long resourceId) throws IOException {
        Resource resource = resourceService.getById(resourceId);
        Query query = Query.query(GridFsCriteria.where("metadata.uuid").is(resource.getUuid()));
        gridFsTemplate.delete(query);
        return new FebsResponse().success();
    }

    @GetMapping("preview")
    @ApiOperation(value = "excel预览")
    public void excel2Pdf(Long resourceId, HttpServletResponse response) throws ValidaException, IOException {
        Resource resource = resourceService.getById(resourceId);
        if (!StringUtils.equals(resource.getSuffix(), ".xls")&&!StringUtils.equals(resource.getSuffix(), ".xlsx")) {
            throw new ValidaException("只支持excel格式数据");
        }
        //加载Excel文档
        Workbook wb = new Workbook();

        Query query = Query.query(GridFsCriteria.where("metadata.uuid").is(resource.getUuid()));
        GridFSFile one = gridFsTemplate.findOne(query);
        GridFsResource fsResource = gridFsTemplate.getResource(one);
        try (InputStream inputStream = fsResource.getInputStream();
             ServletOutputStream outputStream = response.getOutputStream();) {
            wb.loadFromStream(inputStream);
            wb.saveToStream(outputStream, FileFormat.PDF);
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            // chrome浏览器下载文件可能出现：ERR_RESPONSE_HEADERS_MULTIPLE_CONTENT_DISPOSITION，
            // 产生原因：可能是因为文件名中带有英文半角逗号,
            // 解决办法：确保 filename 参数使用双引号包裹[1]
            response.setHeader("Content-Disposition", "attachment; filename=test.pdf");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Expires", "0");
        }
    }

}
