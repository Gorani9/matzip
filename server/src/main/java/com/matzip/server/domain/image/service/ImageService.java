package com.matzip.server.domain.image.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.matzip.server.domain.image.dto.ImageDto;
import com.matzip.server.domain.image.exception.FileDeleteException;
import com.matzip.server.domain.image.exception.FileUploadException;
import com.matzip.server.domain.image.exception.ImageDeleteByUnauthorizedUserException;
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
        if (originalFileName == null)
            return fileName;
        else {
            int extensionIndex = originalFileName.lastIndexOf('.');
            return fileName + originalFileName.substring(extensionIndex);
        }
    }

    public ImageDto.Response uploadImages(ImageDto.UploadRequest uploadRequest) {
        ImageDto.Response response = new ImageDto.Response(new LinkedList<>());
        for (MultipartFile image : uploadRequest.getImages()) {
            Optional<String> imageContentType = Optional.ofNullable(image.getContentType());
            if (imageContentType.isEmpty() || !imageContentType.get().contains("image"))
                throw new UnsupportedFileExtensionException();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(imageContentType.get());
            objectMetadata.setContentLength(image.getSize());
            String fileName = generateFileName(uploadRequest.getUsername(), image.getOriginalFilename());
            try (InputStream inputStream = image.getInputStream()) {
                amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException | SdkClientException e) {
                logger.error(e.getMessage());
                throw new FileUploadException();
            }
            String imageUrl = amazonS3Client.getUrl(bucketName, fileName).toString();
            response.getUrls().add(imageUrl);
        }
        return response;
    }

    private String getUsernameFromKey(String key) {
        int delimIndex = key.lastIndexOf('-');
        return key.substring(0, delimIndex);
    }

    private String getKeyFromUrl(String url) {
        int slashIndex = url.lastIndexOf('/');
        return url.substring(slashIndex + 1);
    }

    public void deleteImages(String username, List<String> urls) {
        for (String url : urls) {
            if (url == null || url.isBlank())
                continue;
            String key = getKeyFromUrl(url);
            String usernameFromKey = getUsernameFromKey(key);
            if (!usernameFromKey.equals(username))
                throw new ImageDeleteByUnauthorizedUserException();
            try {
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
            } catch (SdkClientException e) {
                throw new FileDeleteException();
            }
        }
    }
}
