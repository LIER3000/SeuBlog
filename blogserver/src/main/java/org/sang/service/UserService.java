package org.sang.service;

import org.sang.bean.Role;
import org.sang.bean.User;
import org.sang.config.MyPasswordEncoder;
import org.sang.controller.LoginRegController;
import org.sang.mapper.RolesMapper;
import org.sang.mapper.UserMapper;
import org.sang.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.sql.Timestamp;
import java.util.List;


@Service
@Transactional
public class UserService implements UserDetailsService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    RolesMapper rolesMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userMapper.loadUserByUsername(s);
        if (user == null) {
            //避免返回null，这里返回一个不含有任何值的User对象，在后期的密码比对过程中一样会验证失败
            return new User();
        }
        //查询用户的角色信息，并返回存入user中
        List<Role> roles = rolesMapper.getRolesByUid(user.getId());
        user.setRoles(roles);
        return user;
    }

    /**
     * @param user
     * @return 0表示成功
     * 1表示用户名重复
     * 2表示失败
     */
    @Transactional
    public int reg(User user) {
        // 1. 检查用户名唯一性
        User loadUserByUsername = userMapper.loadUserByUsername(user.getUsername());
        if (loadUserByUsername != null) {
            return 1; // 用户名已存在
        }

        // 3. 密码加密处理
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 4. 设置用户状态和默认属性
        user.setEnabled(true); // 用户可用
        if ("undefined".equals(user.getNickname())) {
            user.setNickname(user.getUsername()); // 设置默认昵称
        }

        user.setUserface("/images/default-avatar.jpg"); // 设置默认头像
        user.setRegTime(new Timestamp(System.currentTimeMillis())); // 设置注册时间

        // 5. 保存用户到数据库
        long result = userMapper.reg(user);
        if (result != 1) {
            return 2; // 用户保存失败
        }

        // 6. 配置用户角色
        String[] roles = new String[]{"2"}; // 默认普通用户
        int rolesResult = rolesMapper.addRoles(roles, user.getId());

        final Logger logger = LoggerFactory.getLogger(UserService.class);
        logger.info("收到注册请求: {}", user.getUsername());
        logger.info("收到注册昵称: {}", user.getNickname());

        // 8. 验证结果并返回
        if (rolesResult == roles.length) {
            return 0; // 注册成功
        } else {
            return 2; // 角色分配失败
        }
    }

    public int updateUserEmail(String email) {
        return userMapper.updateUserEmail(email, Util.getCurrentUser().getId());
    }

    public List<User> getUserByNickname(String nickname) {
        List<User> list = userMapper.getUserByNickname(nickname);
        return list;
    }

    public List<Role> getAllRole() {
        return userMapper.getAllRole();
    }

    public int updateUserEnabled(Boolean enabled, Long uid) {
        return userMapper.updateUserEnabled(enabled, uid);
    }

    public int deleteUserById(Long uid) {
        return userMapper.deleteUserById(uid);
    }

    public int updateUserRoles(Long[] rids, Long id) {
        int i = userMapper.deleteUserRolesByUid(id);
        return userMapper.setUserRoles(rids, id);
    }

    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    //@Override
    public boolean updateUserInfo(Long userId, String nickname, String email) {
        return userMapper.updateUserInfo(userId, nickname, email) > 0;
    }

    //@Override
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        final Logger logger = LoggerFactory.getLogger(UserService.class);
        // 验证当前密码
        String dbPassword = userMapper.getPasswordById(userId);
        if (!passwordEncoder.matches(currentPassword, dbPassword)) {
            return false;
        }
        // 更新为新密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        return userMapper.updatePassword(userId, encodedNewPassword) > 0;
    }
    //@Override
    public boolean updateUserAvatar(Long userId, String avatarUrl) {
        return userMapper.updateUserAvatar(userId, avatarUrl) > 0;
    }
}
