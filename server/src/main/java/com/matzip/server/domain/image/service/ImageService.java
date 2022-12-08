package com.matzip.server.domain.image.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.matzip.server.domain.image.exception.DeleteReviewLastImageException;
import com.matzip.server.domain.image.exception.FileDeleteException;
import com.matzip.server.domain.image.exception.FileUploadException;
import com.matzip.server.domain.image.exception.UnsupportedFileExtensionException;
import com.matzip.server.domain.image.model.ReviewImage;
import com.matzip.server.domain.image.model.UserImage;
import com.matzip.server.domain.image.repository.ReviewImageRepository;
import com.matzip.server.domain.image.repository.UserImageRepository;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReviewImageRepository reviewImageRepository;

    private final UserImageRepository userImageRepository;

    private final AmazonS3Client amazonS3Client;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSSS");

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional(propagation = Propagation.MANDATORY)
    public void uploadUserImage(User user, MultipartFile image) {
        String imageUrl = uploadImage(user.getUsername(), image);
        deleteUserImage(user);
        userImageRepository.save(new UserImage(user, imageUrl));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteUserImage(User user) {
        if (user.getUserImage() != null) {
            deleteImage(user.getUserImage().getImageUrl());
            user.setUserImage(null);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void uploadReviewImages(User user, Review review, List<MultipartFile> images) {
        for (MultipartFile image : images) {
            reviewImageRepository.save(new ReviewImage(review, uploadImage(user.getUsername(), image)));
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteReviewImages(Review review, List<String> urls) {
        if (review.getReviewImages().stream().allMatch(i -> urls.contains(i.getImageUrl())))
            throw new DeleteReviewLastImageException();
        for (String url : urls) {
            deleteImage(url);
        }
        review.deleteImages(urls);
    }

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

    private String uploadImage(String username, MultipartFile image) {
        Optional<String> imageContentType = Optional.ofNullable(image.getContentType());
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

    private void deleteImage(String url) {
        if (url == null || url.isBlank()) return;
        String key = getKeyFromUrl(url);
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (SdkClientException e) {
            throw new FileDeleteException();
        }
    }
}
