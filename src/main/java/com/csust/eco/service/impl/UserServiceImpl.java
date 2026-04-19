package com.csust.eco.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csust.eco.dto.UserLoginDTO;
import com.csust.eco.dto.UserRegisterDTO;
import com.csust.eco.entity.User;
import com.csust.eco.mapper.UserMapper;
import com.csust.eco.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
            throw new RuntimeException("该学号已注册"); // 暂时抛出 RuntimeException，后续可优化为自定义业务异常
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
    public String login(UserLoginDTO loginDTO) {
        // 1. 根据学号查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStudentId, loginDTO.getStudentId());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验密码 (将传入的明文加密后与数据库比对)
        String inputEncrypted = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!user.getPassword().equals(inputEncrypted)) {
            throw new RuntimeException("密码错误");
        }

        // 3. 密码正确，执行 Sa-Token 登录 (核心逻辑)
        // 底层逻辑: Sa-Token 会将当前 userId 存入内部维护的 Token 映射表中, 并生成一个 uuid Token
        StpUtil.login(user.getId());

        // 4. 返回生成的 Token 给前端
        return StpUtil.getTokenValue();
    }
}
