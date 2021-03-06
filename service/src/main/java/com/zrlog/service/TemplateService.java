package com.zrlog.service;

import com.google.gson.Gson;
import com.hibegin.common.util.FileUtils;
import com.hibegin.common.util.StringUtils;
import com.hibegin.common.util.ZipUtil;
import com.jfinal.kit.PathKit;
import com.zrlog.common.Constants;
import com.zrlog.common.response.UpdateRecordResponse;
import com.zrlog.common.response.UploadTemplateResponse;
import com.zrlog.common.vo.TemplateVO;
import com.zrlog.model.WebSite;
import com.zrlog.util.I18NUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TemplateService {

    public UpdateRecordResponse save(String template, Map<String, Object> settingMap) {
        new WebSite().updateByKV(template + Constants.TEMPLATE_CONFIG_SUFFIX, new Gson().toJson(settingMap));
        UpdateRecordResponse updateRecordResponse = new UpdateRecordResponse();
        updateRecordResponse.setMessage("变更成功");
        return updateRecordResponse;
    }

    public UploadTemplateResponse upload(String templateName, File file) throws IOException {
        String finalPath = PathKit.getWebRootPath() + Constants.TEMPLATE_BASE_PATH;
        String finalFile = finalPath + templateName;
        FileUtils.deleteFile(finalFile);
        //start extract template file
        FileUtils.moveOrCopyFile(file.toString(), finalFile, true);
        UploadTemplateResponse response = new UploadTemplateResponse();
        response.setMessage(I18NUtil.getStringFromRes("templateDownloadSuccess"));
        String extractFolder = finalPath + templateName.replace(".zip", "") + "/";
        FileUtils.deleteFile(extractFolder);
        ZipUtil.unZip(finalFile, extractFolder);
        return response;
    }

    public List<TemplateVO> getAllTemplates(String contextPath) {
        String webPath = PathKit.getWebRootPath();
        File[] templatesFile = new File(webPath + Constants.TEMPLATE_BASE_PATH).listFiles();
        List<TemplateVO> templates = new ArrayList<>();
        if (templatesFile != null) {
            for (File file : templatesFile) {
                if (file.isDirectory() && !file.isHidden()) {
                    String templatePath = file.toString().substring(webPath.length()).replace("\\", "/");
                    TemplateVO templateVO = new TemplateVO();
                    File templateInfo = new File(file.toString() + "/template.properties");
                    if (templateInfo.exists()) {
                        Properties properties = new Properties();
                        try (InputStream in = new FileInputStream(templateInfo)) {
                            properties.load(in);
                            templateVO.setAuthor(properties.getProperty("author"));
                            templateVO.setName(properties.getProperty("name"));
                            templateVO.setDigest(properties.getProperty("digest"));
                            templateVO.setVersion(properties.getProperty("version"));
                            templateVO.setUrl(properties.getProperty("url"));
                            if (properties.get("previewImages") != null) {
                                String[] images = properties.get("previewImages").toString().split(",");
                                for (int i = 0; i < images.length; i++) {
                                    String image = images[i];
                                    if (!image.startsWith("https://") && !image.startsWith("http://")) {
                                        images[i] = contextPath + templatePath + "/" + image;
                                    }
                                }
                                templateVO.setPreviewImages(Arrays.asList(images));
                            }
                        } catch (IOException e) {
                            //LOGGER.error("", e);
                        }
                    } else {
                        templateVO.setAuthor("");
                        templateVO.setName(templatePath.substring(Constants.TEMPLATE_BASE_PATH.length()));
                        templateVO.setUrl("");
                        templateVO.setVersion("");
                    }
                    if (templateVO.getPreviewImages() == null || templateVO.getPreviewImages().isEmpty()) {
                        templateVO.setPreviewImages(Collections.singletonList("assets/images/template-default-preview.jpg"));
                    }
                    if (StringUtils.isEmpty(templateVO.getDigest())) {
                        templateVO.setDigest(I18NUtil.getStringFromRes("noIntroduction"));
                    }
                    File settingFile = new File(PathKit.getWebRootPath() + templatePath + "/setting/index.jsp");
                    templateVO.setConfigAble(settingFile.exists());
                    templateVO.setTemplate(templatePath);
                    templates.add(templateVO);
                }
            }
        }

        List<TemplateVO> sortTemplates = new ArrayList<>();
        for (TemplateVO templateVO : templates) {
            if (templateVO.getTemplate().startsWith(Constants.DEFAULT_TEMPLATE_PATH)) {
                templateVO.setDeleteAble(false);
                sortTemplates.add(templateVO);
            } else {
                templateVO.setDeleteAble(true);
            }
        }
        for (TemplateVO templateVO : templates) {
            if (!templateVO.getTemplate().startsWith(Constants.DEFAULT_TEMPLATE_PATH)) {
                sortTemplates.add(templateVO);
            }
        }
        return sortTemplates;
    }
}
