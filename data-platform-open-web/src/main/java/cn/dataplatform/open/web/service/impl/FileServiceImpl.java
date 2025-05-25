package cn.dataplatform.open.web.service.impl;

import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.web.annotation.OperationLog;
import cn.dataplatform.open.web.enums.OperationLogAction;
import cn.dataplatform.open.web.enums.OperationLogFunction;
import cn.dataplatform.open.web.service.FileService;
import cn.dataplatform.open.web.util.AliOSSClient;
import cn.dataplatform.open.web.util.DateUtil;
import cn.dataplatform.open.web.vo.file.FileData;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 文件上传
 *
 * @author dingqianwen
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    /**
     * 允许的文件格式
     */
    @Value("${dp.file.upload.allow-types:.jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx}")
    private List<String> allowFileTypes;

    private AliOSSClient aliOSSClient;

    @Autowired(required = false)
    public void setAliOSSClient(AliOSSClient aliOSSClient) {
        this.aliOSSClient = aliOSSClient;
    }

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @return 文件地址
     */
    @OperationLog(function = OperationLogFunction.FILE,
            action = OperationLogAction.UPLOAD_FILE, requestArg = false)
    @Override
    public FileData upload(MultipartFile multipartFile) {
        if (this.aliOSSClient == null) {
            throw new ApiException("文件上传服务未启用，请检查配置");
        }
        // 不使用原文件的名称
        String originalFilename = multipartFile.getOriginalFilename();
        // 后缀校验
        if (StrUtil.isEmpty(originalFilename)) {
            throw new ApiException("文件名为空");
        }
        if (this.allowFileTypes.stream().noneMatch(originalFilename::endsWith)) {
            throw new ApiException("文件格式不支持");
        }
        String fileExtension = FileUtil.getSuffix(originalFilename);
        String fileName = UUID.fastUUID().toString(true) + "." + fileExtension;
        String fileUrl;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            fileUrl = this.aliOSSClient.upload(inputStream, fileName,
                    // 目录
                    DateUtil.format(new Date(), "yyyy-MM-dd"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ApiException(e.getMessage(), e);
        }
        FileData data = new FileData();
        data.setName(fileName);
        data.setUrl(fileUrl);
        return data;
    }

}
