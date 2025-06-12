<template>
  <el-form :rules="rules" class="login-container" label-position="left" label-width="0px" v-loading="loading" ref="loginForm">
    <h3 class="login_title">系统登录</h3>
    <el-form-item prop="account">
      <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="账号"></el-input>
    </el-form-item>
    <el-form-item prop="checkPass">
      <el-input type="password" v-model="loginForm.password" auto-complete="off" placeholder="密码"></el-input>
    </el-form-item>
    <el-checkbox class="login_remember" v-model="checked" label-position="left">记住密码</el-checkbox>
    <el-form-item style="width: 100%">
      <el-button type="primary" @click.native.prevent="submitClick" style="width: 100%">登录</el-button>
    </el-form-item>


    <!-- 在登录按钮下方添加 -->
    <el-form-item style="width: 100%; margin-top: 10px">
      <el-button
        type="success"
        @click.native.prevent="registerClick"
        style="width: 100%">
        注册新账号
      </el-button>
    </el-form-item>

    <!-- 注册对话框 -->
    <el-dialog
      title="用户注册"
      :visible.sync="registerDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerForm"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="4-20个字符"
            prefix-icon="el-icon-user"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            type="password"
            v-model="registerForm.password"
            show-password
            placeholder="至少8位字符"
            prefix-icon="el-icon-lock"
          ></el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="checkPassword">
          <el-input
            type="password"
            v-model="registerForm.checkPassword"
            show-password
            placeholder="再次输入密码"
            prefix-icon="el-icon-refresh"
          ></el-input>
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
            v-model="registerForm.nickname"
            placeholder="可选"
            prefix-icon="el-icon-edit"
          ></el-input>
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input
            v-model="registerForm.email"
            placeholder="可选"
            prefix-icon="el-icon-message"
          ></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitRegister"
          :loading="registerLoading"
        >
          注册
        </el-button>
      </div>
    </el-dialog>

  </el-form>
</template>
<script>
  import {postRequest} from '../utils/api'
  import {putRequest} from '../utils/api'
  export default{
    data(){
      return {
        rules: {
          // account: [{required: true, message: '请输入用户名', trigger: 'none'}],
          // checkPass: [{required: true, message: '请输入密码', trigger: 'none'}]
        },
        checked: true,
        loginForm: {
          username: 'sang',
          password: '123'
        },
        loading: false,

        // 注册对话框控制
        registerDialogVisible: false,
        registerLoading: false,

        // 注册表单数据
        registerForm: {
          username: '',
          password: '',
          checkPassword: '',
          nickname: '',
          email: '',
          agreement: false
        },

        // 注册表单验证规则
        registerRules: {
          username: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
            { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' },
            { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '请输入密码', trigger: 'blur' },
            { min: 8, message: '密码长度至少8位', trigger: 'blur' }
          ],
          checkPassword: [
            { required: true, message: '请确认密码', trigger: 'blur' },
            { validator: this.validatePassword, trigger: 'blur' }
          ]
        }

      }
    },
    methods: {
      submitClick: function () {
        var _this = this;
        this.loading = true;
        postRequest('/login', {
          username: this.loginForm.username,
          password: this.loginForm.password
        }).then(resp=> {
          _this.loading = false;
          if (resp.status == 200) {
            //成功
            var json = resp.data;
            if (json.status == 'success') {
              _this.$router.replace({path: '/home'});
            } else {
              _this.$alert('登录失败!', '失败!');
            }
          } else {
            //失败
            _this.$alert('登录失败!', '失败!');
          }
        }, resp=> {
          _this.loading = false;
          _this.$alert('找不到服务器⊙﹏⊙∥!', '失败!');
        });
      },

      // 修改后的注册方法 - 弹出注册对话框
      registerClick() {
        this.resetRegisterForm();
        this.registerDialogVisible = true;
      },

      // 密码确认验证
      validatePassword(rule, value, callback) {
        if (value !== this.registerForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },

      // 重置注册表单
      resetRegisterForm() {
        this.registerForm = {
          username: '1234',
          password: '12341234',
          checkPassword: '12341234',
          nickname: '',
          email: '',
          agreement: false
        };

        // 清除验证状态
        if (this.$refs.registerForm) {
          this.$refs.registerForm.clearValidate();
        }
      },

      // 提交注册表单
      submitRegister() {
        this.$refs.registerForm.validate(valid => {
          if (!valid) {
            this.$message.error('请完善注册信息');
            return false;
          }

          this.registerLoading = true;

          // 构建注册数据
          const registerData = {
            username: this.registerForm.username,
            password: this.registerForm.password,
            nickname: this.registerForm.nickname || undefined,
            email: this.registerForm.email || undefined
          };

          // 发送注册请求
          postRequest('/reg', registerData)
            .then(response => {
              this.registerLoading = false;
              if (!(response && response.data)) {
                this.$message.error('服务器返回空响应');
                return;
              }
              const resData = response.data;
              // 检查必要字段
              if (typeof resData.code === 'undefined' || typeof resData.message === 'undefined') {
                this.$message.error('服务器响应格式异常');
                return;
              }
              // 处理响应
              if (resData.code === 0) {
                this.$message.success('注册成功！用户ID：' + (resData.userId || ''));
                //this.$router.push('/login');
                this.registerDialogVisible = false;
              } else {
                this.$message.error('注册失败：' + resData.message);
              }
            })
            .catch(error => {
              this.registerLoading = false;
              this.$message.error('请求失败：' + error.message);
            });
        });
      }
    }
  }
</script>
<style>
  .login-container {
    border-radius: 15px;
    background-clip: padding-box;
    margin: 180px auto;
    width: 350px;
    padding: 35px 35px 15px 35px;
    background: #fff;
    border: 1px solid #eaeaea;
    box-shadow: 0 0 25px #cac6c6;
  }

  .login_title {
    margin: 0px auto 40px auto;
    text-align: center;
    color: #505458;
  }

  .login_remember {
    margin: 0px 0px 35px 0px;
    text-align: left;
  }

  .dialog-footer {
    text-align: center;
  }

</style>
