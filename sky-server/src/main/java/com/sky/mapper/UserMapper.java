package com.sky.mapper;

import com.sky.dto.DailyUserDTO;
import com.sky.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户数据
     */
    void insert(User user);

    /**
     * 根据id查询用户
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 查询每日新增用户数
     */
    List<DailyUserDTO> selectDailyNewUsers(
            @Param("begin") LocalDateTime begin,
            @Param("end") LocalDateTime end
    );
}