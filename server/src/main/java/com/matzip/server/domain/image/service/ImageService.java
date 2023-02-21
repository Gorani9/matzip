package com.matzip.server.domain.image.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.matzip.server.domain.image.exception.FileDeleteException;
import com.matzip.server.domain.image.exception.FileUploadException;
import com.matzip.server.domain.image.exception.UnsupportedFileExtensionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    private final AmazonS3 amazonS3;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSSS");

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private String generateFileName(String username, String originalFileName) {
        return username + "-" + (originalFileName != null ? originalFileName + "-" : "") + simpleDateFormat.format(new Date());
    }

    public String uploadImage(String username, MultipartFile image) {
        Optional<String> imageContentType = Optional.ofNullable(image.getContentType());
        if (imageContentType.isEmpty() || !imageContentType.get().contains("image")) throw new UnsupportedFileExtensionException();

        String fileName = generateFileName(username, image.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(imageContentType.get());
        objectMetadata.setContentLength(image.getSize());

        try (InputStream inputStream = image.getInputStream()) {
            amazonS3.putObject(
                    new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata).withCannedAcl(
                            CannedAccessControlList.PublicRead));
        } catch (SdkClientException | IOException e) {
            log.error("File Upload Error", e);
            throw new FileUploadException();
        }
        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    public List<String> uploadImages(String username, List<MultipartFile> images) {
        ArrayList<String> uploadedImages = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                uploadedImages.add(uploadImage(username, image));
            }
        } catch (FileUploadException e) {
            for (String uploadedImage : uploadedImages) {
                deleteImage(uploadedImage);
            }
            throw e;
        }
        return uploadedImages;
    }

    public String deleteImage(String url) {
        String key = url.substring(url.lastIndexOf('/') + 1);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (SdkClientException e) {
            log.error("File Delete Error", e);
            throw new FileDeleteException();
        }
        return url;
    }

    public List<String> deleteImages(List<String> urls) {
        ArrayList<String> deletedImages = new ArrayList<>();
        try {
            for (String url : urls) {
                deletedImages.add(deleteImage(url));
            }
        } catch (FileDeleteException ignored) {}
        return deletedImages;
    }
}
