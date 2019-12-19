/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.R;
import io.renren.modules.sys.dao.SysUserTokenDao;
import io.renren.modules.sys.entity.SysUserTokenEntity;
import io.renren.modules.sys.oauth2.TokenGenerator;
import io.renren.modules.sys.service.SysUserTokenService;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service("sysUserTokenService")
public class SysUserTokenServiceImpl extends ServiceImpl<SysUserTokenDao, SysUserTokenEntity> implements SysUserTokenService {

    // 12小时后过期
    private final static int EXPIRE = 3600 * 12;

    @Override
    public R createToken(long userId) {
        // 生成一个 token
        String token = TokenGenerator.generateValue();

        // 当前时间
        Date now = new Date();
        // 过期时间
        Date expireTime = new Date(now.getTime() + EXPIRE * 1000);

        // 判断是否生成过 token
        SysUserTokenEntity tokenEntity = this.getById(userId);
        if (tokenEntity == null) { // 新增 SysUserTokenEntity
            tokenEntity = new SysUserTokenEntity();
            tokenEntity.setUserId(userId);
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);

            // 保存 token
            this.save(tokenEntity);
        } else { // 更新 SysUserTokenEntity
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);

            // 更新 token
            this.updateById(tokenEntity);
        }

        // 返回 token 和过期时间
        return R.ok().put("token", token).put("expire", EXPIRE);
    }

    @Override
    public void logout(long userId) {
        // 生成一个token
        String token = TokenGenerator.generateValue();

        // 修改token
        SysUserTokenEntity tokenEntity = new SysUserTokenEntity();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(token);
        this.updateById(tokenEntity);
    }
}
