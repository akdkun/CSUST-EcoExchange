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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO registerDTO) {
        // 1. 前置过滤（防御性编程）：拦截 99% 的普通重复请求，减轻数据库索引压力
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStudentId, registerDTO.getStudentId());
        if (this.count(queryWrapper) > 0) {
            throw new BizException("该学号已注册");
        }

        // 2. 构建实体
        User user = new User();
        user.setStudentId(registerDTO.getStudentId());
        user.setNickname(registerDTO.getNickname());

        // 密码处理：建议至少在 MD5 之上加一个盐值 (Salt)，防止彩虹表攻击
        String salt = "csust_eco_";
        user.setPassword(DigestUtil.md5Hex(salt + registerDTO.getPassword()));

        // 3. 核心重构：利用数据库唯一索引进行兜底
        try {
            // 执行到这一行时，MySQL 会进行最后的唯一性裁决
            this.save(user);
        } catch (DuplicateKeyException e) {
            // 如果物理插入失败，说明在 Step 1 和 Step 3 之间有并发请求抢跑了
            log.warn("检测到并发注册冲突，学号: {}", registerDTO.getStudentId());
            throw new BizException("该学号已注册，请直接登录");
        }
    }

    @Override
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
