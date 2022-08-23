package com.matzip.server.domain.image.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.matzip.server.domain.image.exception.FileDeleteException;
import com.matzip.server.domain.image.exception.FileUploadException;
import com.matzip.server.domain.image.exception.UnsupportedFileExtensionException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AmazonS3Client amazonS3Client;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSSS");

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private String generateFileName(String username, String originalFileName) {
        String fileName = username + "-" + simpleDateFormat.format(new Date());
        if (originalFileName == null) return fileName;
        else {
            int extensionIndex = originalFileName.lastIndexOf('.');
            if (extensionIndex == -1)
                throw new UnsupportedFileExtensionException();
            return fileName + originalFileName.substring(extensionIndex);
        }
    }

    public List<String> uploadImages(String username, List<MultipartFile> images) {
        List<String> imageUrls = new LinkedList<>();
        for (MultipartFile image : images) {
            imageUrls.add(uploadImage(username, image));
        }
        return imageUrls;
    }

    public String uploadImage(String username, MultipartFile image) {
        Optional<String> imageContentType = Optional.ofNullable(image.getContentType());
        logger.error(imageContentType.orElse("EMPTY!"));
        if (imageContentType.isEmpty())
            throw new UnsupportedFileExtensionException();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(imageContentType.get());
        objectMetadata.setContentLength(image.getSize());
        String fileName = generateFileName(username, image.getOriginalFilename());
        try (InputStream inputStream = image.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata).withCannedAcl(
                            CannedAccessControlList.PublicRead));
        } catch (IOException | SdkClientException e) {
            logger.error(e.getMessage());
            throw new FileUploadException();
        }
        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    private String getKeyFromUrl(String url) {
        int slashIndex = url.lastIndexOf('/');
        return url.substring(slashIndex + 1);
    }

    public void deleteImages(List<String> urls) {
        for (String url : urls) {
            deleteImage(url);
        }
    }

    public void deleteImage(String url) {
        if (url == null || url.isBlank()) return;
        String key = getKeyFromUrl(url);
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (SdkClientException e) {
            throw new FileDeleteException();
        }
    }
}
