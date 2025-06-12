package org.sang.controller;

import org.sang.bean.RespBean;
import org.sang.bean.User;
import org.sang.service.UserService;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @RequestMapping("/currentUserName")
    public String currentUserName() {
        return Util.getCurrentUser().getNickname();
    }

    @RequestMapping("/currentUserId")
    public Long currentUserId() {
        return Util.getCurrentUser().getId();
    }

    @RequestMapping("/currentUserEmail")
    public String currentUserEmail() {
        return Util.getCurrentUser().getEmail();
    }

    @RequestMapping("/isAdmin")
    public Boolean isAdmin() {
        List<GrantedAuthority> authorities = Util.getCurrentUser().getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().contains("超级管理员")) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/updateUserEmail",method = RequestMethod.PUT)
    public RespBean updateUserEmail(String email) {
        if (userService.updateUserEmail(email) == 1) {
            return new RespBean("success", "开启成功!");
        }
        return new RespBean("error", "开启失败!");
    }

    // 新增：用户信息接口
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public Map<String, Object> getUserInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取当前认证用户
            User currentUser = Util.getCurrentUser();
            if (currentUser == null) {
                response.put("status", "error");
                response.put("msg", "用户未登录");
                return response;
            }

            // 构建用户数据
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", currentUser.getId());
            userData.put("username", currentUser.getUsername());
            userData.put("nickname", currentUser.getNickname());
            userData.put("email", currentUser.getEmail());
            userData.put("avatar", currentUser.getUserface());
            userData.put("regTime", formatTimestamp(currentUser.getRegTime()));
            userData.put("enabled", currentUser.isEnabled());

            // 添加角色信息
            List<GrantedAuthority> authorities = Util.getCurrentUser().getAuthorities();
            String[] roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);
            userData.put("roles", roles);

            response.put("status", "success");
            response.put("msg", "用户信息获取成功");
            response.put("data", userData);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("msg", "获取用户信息失败: " + e.getMessage());
        }
        return response;
    }

    // 新增：更新用户信息接口
    @RequestMapping(value = "/updateUserInfo", method = RequestMethod.PUT)
    public RespBean updateUserInfo(String nickname, String email) {
        try {
            // 获取当前认证用户
            User currentUser = Util.getCurrentUser();
            if (currentUser == null) {
                return new RespBean("error", "用户未登录");
            }

            // 更新用户信息
            if (userService.updateUserInfo(currentUser.getId(), nickname, email)) {
                // 更新本地缓存信息
                currentUser.setNickname(nickname);
                currentUser.setEmail(email);
                return new RespBean("success", "用户信息更新成功");
            }
            return new RespBean("error", "用户信息更新失败");
        } catch (Exception e) {
            return new RespBean("error", "更新信息失败: " + e.getMessage());
        }
    }

    // 新增：更新用户密码接口
    @RequestMapping(value = "/updatePassword", method = RequestMethod.PUT)
    public RespBean updatePassword(String currentPassword, String newPassword) {
        try {
            // 获取当前认证用户
            User currentUser = Util.getCurrentUser();
            if (currentUser == null) {
                return new RespBean("error", "用户未登录");
            }

            // 更新密码
            if (userService.updatePassword(currentUser.getId(), currentPassword, newPassword)) {
                return new RespBean("success", "密码更新成功");
            }
            return new RespBean("error", "密码更新失败：原密码不正确");
        } catch (Exception e) {
            return new RespBean("error", "密码更新失败: " + e.getMessage());
        }
    }

    // 新增：更新用户头像接口
    @PostMapping("/updateAvatar")
    public RespBean handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new RespBean("error", "上传文件为空");
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String newFilename = UUID.randomUUID() + extension;

            // 创建目标文件
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问 URL
            String fileUrl = "/updateAvatar/" + newFilename;

            return new RespBean("success", fileUrl);
        } catch (Exception e) {
            return new RespBean("error", "上传失败: " + e.getMessage());
        }
    }

    // 时间格式化工具方法
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp.getTime()));
    }
}
