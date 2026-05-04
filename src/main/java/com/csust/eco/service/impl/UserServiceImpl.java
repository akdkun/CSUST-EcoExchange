package com.csust.eco.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csust.eco.common.BizException;
import com.csust.eco.dto.UserLoginDTO;
import com.csust.eco.dto.UserRegisterDTO;
import com.csust.eco.entity.User;
import com.csust.eco.mapper.UserMapper;
import com.csust.eco.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csust.eco.vo.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    @Transactional
    public void register(UserRegisterDTO registerDTO) {
        // 1. 检查学号是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStudentId, registerDTO.getStudentId());
        if (this.count(queryWrapper) > 0) {
            throw new BizException("该学号已注册");
        }

        // 2. 密码加密 (明文密码 -> MD5)
        // 底层逻辑: 严禁明文存储密码. 实际生产中最好加盐(Salt), 这里为了快速跑通V1.0, 采用标准MD5
        String encryptedPassword = DigestUtil.md5Hex(registerDTO.getPassword());

        // 3. 构建实体并保存
        User user = new User();
        user.setStudentId(registerDTO.getStudentId());
        user.setPassword(encryptedPassword);
        user.setNickname(registerDTO.getNickname());

        this.save(user);
    }

    @Override
    @Transactional
    public UserInfoVO login(UserLoginDTO loginDTO) { // 修改点 1: 返回值改为 UserInfoVO
        // 1. 根据学号查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStudentId, loginDTO.getStudentId());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 2. 校验密码
        String inputEncrypted = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!user.getPassword().equals(inputEncrypted)) {
            throw new BizException("密码错误");
        }

        // 3. 密码正确，执行 Sa-Token 登录
        StpUtil.login(user.getId());

        // 4. 构建 VO (安全隔离层)
        UserInfoVO userInfoVO = new UserInfoVO();
        // 将 Entity 中的同名属性 (id, studentId, nickname, avatar) 自动拷贝到 VO 中
        // 密码 (password) 字段在 VO 中不存在, 因此自动被丢弃, 实现了数据脱敏
        BeanUtil.copyProperties(user, userInfoVO);

        // 5. 将 Token 注入到 VO 中一并返回
        userInfoVO.setTokenValue(StpUtil.getTokenValue());

        return userInfoVO;
    }
}
