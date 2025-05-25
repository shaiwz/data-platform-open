package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.RateLimit;
import cn.dataplatform.open.web.enums.RateLimitStrategy;
import cn.dataplatform.open.web.service.FileService;
import cn.dataplatform.open.web.vo.file.FileData;
import cn.dataplatform.open.web.annotation.RateLimit;
import cn.dataplatform.open.web.enums.RateLimitStrategy;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.temporal.ChronoUnit;

/**
 * 文件上传
 *
 * @author dingqianwen
 */
@RestController
@RequestMapping(path = "file")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @return 文件地址
     */
    @RateLimit(type = RateLimitStrategy.USER, limit = 10,
            refreshInterval = 5, chronoUnit = ChronoUnit.MINUTES)
    @PostMapping(path = "upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public PlainResult<FileData> upload(@RequestPart(name = "file") MultipartFile multipartFile) {
        FileData fileData = this.fileService.upload(multipartFile);
        return new PlainResult<>(fileData);
    }

}
