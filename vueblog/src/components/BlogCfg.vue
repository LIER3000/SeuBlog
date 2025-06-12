<template>
  <el-card v-loading="loading" style="width: 700px; margin: 0 auto;">
    <div slot="header" class="card-header">
      <span style="font-size: 18px; font-weight: bold;">个人资料设置</span>
    </div>

    <div class="profile-container">
<!--      &lt;!&ndash; 左侧头像区 &ndash;&gt;-->
<!--      <div class="avatar-section">-->
<!--        <el-avatar :size="120" :src="user.avatar" shape="circle"></el-avatar>-->
<!--        <div class="avatar-actions">-->
<!--          <input-->
<!--            type="file"-->
<!--            ref="avatarInput"-->
<!--            style="display: none"-->
<!--            @change="handleAvatarChange"-->
<!--            accept="image/*"-->
<!--          >-->
<!--          <el-button-->
<!--            type="primary"-->
<!--            size="small"-->
<!--            icon="el-icon-upload"-->
<!--            @click="$refs.avatarInput.click()"-->
<!--          >-->
<!--            更换头像-->
<!--          </el-button>-->
<!--          <p style="color: #909399; font-size: 12px; margin-top: 8px;">-->
<!--            支持 JPG/PNG 格式，最大5MB-->
<!--          </p>-->
<!--        </div>-->
<!--      </div>-->

      <!-- 右侧信息区 -->
      <div class="info-section">
        <el-form :model="user" label-width="90px" size="small">
          <!-- 用户基本信息 -->
          <div class="form-group">
            <div class="form-header">
              <i class="el-icon-user-solid"></i>
              <span class="section-title">基本信息</span>
            </div>

            <el-form-item label="用户名">
              <el-input v-model="user.username" disabled></el-input>
            </el-form-item>

            <el-form-item label="用户ID">
              <el-input v-model="user.id" disabled></el-input>
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="user.nickname" placeholder="请输入昵称"></el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="user.email"
                type="email"
                placeholder="请输入邮箱"
                @blur="validateEmail"
              ></el-input>
              <div
                v-if="emailError"
                style="color: #f56c6c; font-size: 12px; margin-top: 5px;"
              >
                {{ emailError }}
              </div>
            </el-form-item>

            <el-form-item label="注册时间">
              <el-input v-model="user.regTime" disabled></el-input>
            </el-form-item>

            <el-form-item label="用户状态">
              <el-tag :type="user.enabled ? 'success' : 'danger'" size="medium">
                {{ user.enabled ? '已激活' : '已禁用' }}
              </el-tag>
            </el-form-item>

            <!-- 保存基本信息按钮 -->
            <div style="text-align: center; margin-top: 15px;">
              <el-button
                type="primary"
                @click="saveBasicInfo"
                :loading="basicSaving"
                style="width: 60%;"
              >
                保存基本信息
              </el-button>
            </div>
          </div>

          <!-- 密码修改 -->
          <div class="form-group" style="margin-top: 30px; border-top: 1px solid #ebeef5; padding-top: 20px;">
            <div class="form-header">
              <i class="el-icon-lock"></i>
              <span class="section-title">密码修改</span>
            </div>

            <el-form-item label="当前密码">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              ></el-input>
            </el-form-item>

            <el-form-item label="新密码">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
                @input="validatePassword"
              ></el-input>
              <div
                v-if="passwordError"
                style="color: #f56c6c; font-size: 12px; margin-top: 5px;"
              >
                {{ passwordError }}
              </div>
            </el-form-item>

            <el-form-item label="确认密码">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                @blur="validatePasswordConfirmation"
              ></el-input>
              <div
                v-if="confirmPasswordError"
                style="color: #f56c6c; font-size: 12px; margin-top: 5px;"
              >
                {{ confirmPasswordError }}
              </div>
            </el-form-item>

            <!-- 保存密码按钮 -->
            <div style="text-align: center; margin-top: 20px;">
              <el-button
                type="primary"
                @click="savePassword"
                :loading="passwordSaving"
                style="width: 60%;"
              >
                更新密码
              </el-button>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </el-card>
</template>

<script>
import {getRequest, putRequest, uploadAvatar} from '../utils/api'
import axios from "axios";

export default {
  data() {
    return {
      loading: false,
      basicSaving: false, // 基本信息保存状态
      passwordSaving: false, // 密码保存状态
      emailError: '',
      passwordError: '',
      confirmPasswordError: '',
      user: {
        id: '',
        username: '',
        nickname: '',
        email: '',
        avatar: '',
        regTime: '',
        enabled: true
      },
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  mounted() {
    this.loadUserInfo();
  },
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      this.loading = true;
      try {
        const response = await getRequest("/userInfo");
        if (response.status === 200 && response.data) {
          const userData = response.data.data || response.data;
          this.user = {
            id: userData.id,
            username: userData.username,
            nickname: userData.nickname,
            email: userData.email,
            avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
            regTime: userData.regTime ? new Date(userData.regTime).toLocaleString() : "未知时间",
            enabled: userData.enabled
          };
        }
      } catch (error) {
        this.$message.error('加载用户信息失败');
        console.error("加载用户信息失败:", error);
      } finally {
        this.loading = false;
      }
    },

    // 处理头像更改
    async handleAvatarChange(e) {
      const file = e.target.files[0];
      if (!file || !file.type.match('image.*')) {
        this.$message.error('请选择有效的图片文件');
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.$message.error('图片大小不能超过5MB');
        return;
      }

      // 创建FormData对象用于文件上传
      const formData = new FormData();
      formData.append('avatar', file);

      try {
        this.loading = true;

        // 调用新的文件上传接口
        const uploadResponse = await axios.post("updateAvatar", formData);

        if (uploadResponse.status !== 200 || !uploadResponse.data.msg) {
          this.$message.error('头像上传失败:' + uploadResponse.status);
        }

        this.$message.success('头像上传成功');

        // 2. 用获取到的 URL 更新头像
        const response = await getRequest("/userInfo");
        if (response.status === 200 && response.data) {
          const userData = response.data.data || response.data;
          this.user.avatar = userData.avatar;
          this.$message.success('头像更新成功');
        }
      } catch (error) {
        console.error("头像下载失败:", error);
        this.$message.error("头像下载失败:" + error.message);
      } finally {
        this.loading = false;
        e.target.value = '';
      }
    },

    // 验证邮箱
    validateEmail() {
      if (!this.user.email) {
        this.emailError = '';
        return true;
      }

      const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (regex.test(this.user.email)) {
        this.emailError = '';
        return true;
      } else {
        this.emailError = '邮箱格式不正确';
        return false;
      }
    },

    // 验证密码
    validatePassword() {
      if (!this.passwordForm.newPassword) {
        this.passwordError = '';
        return true;
      }

      if (this.passwordForm.newPassword.length < 8) {
        this.passwordError = '密码长度至少8位';
        return false;
      } else {
        this.passwordError = '';
        return true;
      }
    },

    // 验证密码确认
    validatePasswordConfirmation() {
      if (!this.passwordForm.confirmPassword) {
        this.confirmPasswordError = '';
        return true;
      }

      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.confirmPasswordError = '两次输入的密码不一致';
        return false;
      } else {
        this.confirmPasswordError = '';
        return true;
      }
    },

    // 保存基本信息
    async saveBasicInfo() {
      // 只验证邮箱
      if (!this.validateEmail()) {
        this.$message.warning('请修正邮箱格式错误');
        return;
      }

      this.basicSaving = true;

      try {
        // 准备需要更新的数据
        const updateData = {
          nickname: this.user.nickname,
          email: this.user.email
        };

        // 发送更新请求
        const response = await putRequest("/updateUserInfo", updateData);

        if (response.status === 200) {
          this.$message.success('基本信息已更新');
        } else {
          const errorMsg = response.data && response.data.msg || '保存失败';
          this.$message.error(errorMsg);
        }
      } catch (error) {
        console.error("保存失败:", error);
        this.$message.error('保存失败，请重试');
      } finally {
        this.basicSaving = false;
      }
    },

    // 保存密码
    async savePassword() {
      // 密码验证
      if (!this.validatePassword()) {
        this.$message.warning('密码长度至少8位');
        return;
      }

      if (!this.validatePasswordConfirmation()) {
        this.$message.warning('两次输入的密码不一致');
        return;
      }

      if (!this.passwordForm.currentPassword) {
        this.$message.warning('请输入当前密码');
        return;
      }

      this.passwordSaving = true;

      try {
        // 准备密码更新数据
        const passwordData = {
          currentPassword: this.passwordForm.currentPassword,
          newPassword: this.passwordForm.newPassword
        };

        // 发送更新请求
        const response = await putRequest("/updatePassword", passwordData);

        if (response.status === 200 && response.data.status === 'success') {
          this.$message.success('密码更新成功');

          // 清除密码表单
          this.passwordForm = {
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          };
        } else {
          const errorMsg = response.data && response.data.msg || '密码更新失败';
          this.$message.error(errorMsg);
        }
      } catch (error) {
        console.error("保存失败:", error);
        this.$message.error('密码更新失败，请重试');
      } finally {
        this.passwordSaving = false;
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  display: flex;
  padding: 10px;
}

.card-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.avatar-section {
  flex: 0 0 150px;
  padding: 15px;
  text-align: center;
}

.info-section {
  flex: 1;
  padding: 10px;
}

.avatar-actions {
  margin-top: 15px;
}

.form-group {
  margin-bottom: 20px;
}

.form-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  font-weight: bold;
}

.section-title {
  margin-left: 8px;
  color: #1e5799;
  font-size: 16px;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-tag {
  margin-right: 10px;
}

/* 响应式设计 - 在小屏幕上堆叠布局 */
@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }

  .avatar-section {
    margin-bottom: 20px;
  }
}
</style>
