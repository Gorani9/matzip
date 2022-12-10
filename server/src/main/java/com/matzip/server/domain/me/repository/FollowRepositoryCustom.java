package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import org.springframework.data.domain.Slice;

public interface FollowRepositoryCustom {
    Slice<User> searchMyFollowersByUsername(UserDto.SearchRequest searchRequest, Long myId);
    Slice<User> searchMyFollowingsByUsername(UserDto.SearchRequest searchRequest, Long myId);
}
