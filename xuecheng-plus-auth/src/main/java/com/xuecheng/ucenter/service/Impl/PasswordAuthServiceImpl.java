package com.xuecheng.ucenter.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    // 密码工具类
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 检验验证码
        // 根据用户名获取db信息，检验账户是否存在
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if(xcUser == null){
            throw new RuntimeException("用户名不存在");
        }

        // 校验密码
        String passwordDb = xcUser.getPassword();
        String passwordAuth = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordAuth, passwordDb);
        if(!matches){
            throw new RuntimeException("密码错误");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }
}
