package com.matzip.server.domain.image.repository;

import com.matzip.server.domain.image.model.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, Long> {
}
