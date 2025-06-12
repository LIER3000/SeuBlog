package org.sang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sang.bean.Role;
import org.sang.bean.User;

import java.util.List;


@Mapper
public interface UserMapper {

    User loadUserByUsername(@Param("username") String username);

    long reg(User user);

    int updateUserEmail(@Param("email") String email, @Param("id") Long id);

    List<User> getUserByNickname(@Param("nickname") String nickname);

    List<Role> getAllRole();

    int updateUserEnabled(@Param("enabled") Boolean enabled, @Param("uid") Long uid);

    int deleteUserById(Long uid);

    int deleteUserRolesByUid(Long id);

    int setUserRoles(@Param("rids") Long[] rids, @Param("id") Long id);

    User getUserById(@Param("id") Long id);

    int updateUserInfo(
            @Param("id") Long id,
            @Param("nickname") String nickname,
            @Param("email") String email
    );

    // 更新用户密码
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    // 获取用户密码
    String getPasswordById(@Param("id") Long id);

    // 更新用户头像
    int updateUserAvatar(@Param("id") Long id, @Param("avatar") String avatar);
}
