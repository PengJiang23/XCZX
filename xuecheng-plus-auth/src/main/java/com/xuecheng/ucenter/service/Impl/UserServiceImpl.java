package com.xuecheng.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Component
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Autowired
    XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {


        // 传入的认证信息为一个模型类，不是username+password格式，需要解析为obj
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不正确");
        }
        String checkcodekey = authParamsDto.getCheckcodekey();
        String checkcode = authParamsDto.getCheckcode();

        if(StringUtils.isEmpty(checkcodekey) || StringUtils.isEmpty(checkcode)){
            throw new RuntimeException("请输入验证码");
        }

        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        // 这里还要判断null是因为降级服务中返回的是null
        if (!verify || verify ==null) {
            throw new RuntimeException("验证码错误");
        }

        String authType = authParamsDto.getAuthType();
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        UserDetails userPrincipal = getUserPrincipal(xcUserExt);
        return userPrincipal;

    }

    public UserDetails getUserPrincipal(XcUserExt xcUserExt) {
        String password = xcUserExt.getPassword();
        String[] authorities = {"test"};
        String userId = xcUserExt.getId();
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);

        if(xcMenus.size()>0){
            ArrayList<String> permissions = new ArrayList<>();
            xcMenus.forEach(m->{
                permissions.add(m.getCode());
            });

            authorities = permissions.toArray(new String[0]);
        }
        // jwt想要携带更多的信息：1.修改userdetails中的源文件2.下面username扩充然后转为json格式
        xcUserExt.setPassword(null);
        String userJson = JSON.toJSONString(xcUserExt);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
        return userDetails;
    }

}
