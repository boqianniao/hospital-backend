package com.hospital.third.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.hospital.common.exception.BusinessException;
import com.hospital.config.props.HospitalProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 对象存储：文件上传。
 * 仅当 hospital.oss.enabled=true 且 endpoint/accessKey/secretKey/bucket 齐全时初始化客户端；
 * 否则 isReady()=false，上传接口返回“未启用”，应用照常启动。
 */
@Slf4j
@Service
public class OssService {

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private final HospitalProperties props;
    private OSS client;
    private volatile boolean ready = false;

    public OssService(HospitalProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        HospitalProperties.Oss cfg = props.getOss();
        if (!cfg.isEnabled()) {
            log.info("[OSS] 未启用（hospital.oss.enabled=false），文件上传不可用");
            return;
        }
        if (!StringUtils.hasText(cfg.getEndpoint()) || !StringUtils.hasText(cfg.getAccessKey())
                || !StringUtils.hasText(cfg.getSecretKey()) || !StringUtils.hasText(cfg.getBucket())) {
            log.warn("[OSS] 已启用但配置不完整（endpoint/accessKey/secretKey/bucket），上传不可用");
            return;
        }
        try {
            this.client = new OSSClientBuilder().build(cfg.getEndpoint(), cfg.getAccessKey(), cfg.getSecretKey());
            this.ready = true;
            log.info("[OSS] 初始化完成 endpoint={} bucket={}", cfg.getEndpoint(), cfg.getBucket());
        } catch (Exception e) {
            log.error("[OSS] 初始化失败，上传不可用: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            client.shutdown();
        }
    }

    public boolean isReady() {
        return ready;
    }

    /**
     * 上传文件，返回可访问的公网 URL。
     * @param file 上传文件
     * @param dir  业务目录前缀（如 avatar、feedback），可空
     */
    public String upload(MultipartFile file, String dir) {
        if (!ready) {
            throw new BusinessException("对象存储未启用，无法上传");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        HospitalProperties.Oss cfg = props.getOss();
        String key = buildKey(dir, file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            if (StringUtils.hasText(file.getContentType())) {
                meta.setContentType(file.getContentType());
            }
            client.putObject(cfg.getBucket(), key, in, meta);
            return buildUrl(cfg.getEndpoint(), cfg.getBucket(), key);
        } catch (IOException e) {
            log.error("[OSS] 读取上传流失败 key={}: {}", key, e.getMessage());
            throw new BusinessException("文件上传失败，请稍后重试");
        } catch (Exception e) {
            log.error("[OSS] 上传失败 key={}: {}", key, e.getMessage(), e);
            throw new BusinessException("文件上传失败，请稍后重试");
        }
    }

    /** 生成对象 key：{dir}/{yyyyMM}/{uuid}.{ext} */
    private String buildKey(String dir, String originalName) {
        String ext = "";
        if (StringUtils.hasText(originalName)) {
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) {
                ext = originalName.substring(dot);
            }
        }
        String prefix = StringUtils.hasText(dir) ? dir.replaceAll("^/+|/+$", "") + "/" : "upload/";
        return prefix + LocalDate.now().format(MONTH) + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    /** 由 region endpoint + bucket 拼接公网访问 URL：https://{bucket}.{host}/{key} */
    private String buildUrl(String endpoint, String bucket, String key) {
        String scheme = "https";
        String host = endpoint;
        int idx = endpoint.indexOf("://");
        if (idx > 0) {
            scheme = endpoint.substring(0, idx);
            host = endpoint.substring(idx + 3);
        }
        host = host.replaceAll("/+$", "");
        return scheme + "://" + bucket + "." + host + "/" + key;
    }
}
