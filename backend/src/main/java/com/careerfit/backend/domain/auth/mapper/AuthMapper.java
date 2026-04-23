package com.careerfit.backend.domain.auth.mapper;

import com.careerfit.backend.domain.user.entity.Users;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthMapper {

    boolean existsByEmail(String email);

    void save(Users user);

    Users findByEmail(String email);

    Users findById(Long id);
}
