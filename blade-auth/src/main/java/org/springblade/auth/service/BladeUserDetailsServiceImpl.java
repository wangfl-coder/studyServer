/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.auth.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springblade.auth.constant.AuthConstant;
import org.springblade.auth.utils.TokenUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.core.tool.utils.WebUtil;
import org.springblade.system.entity.Tenant;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.entity.UserInfo;
import org.springblade.system.user.enums.UserEnum;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户信息
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class BladeUserDetailsServiceImpl implements UserDetailsService {

	private final IUserClient userClient;
	private final ISysClient sysClient;

	private static final String REFRESHTOKEN_GRANT_TYPE = "refresh_token";
	private static final String PASSWORD_GRANT_TYPE = "password";
	private static final String CAPTCHA_GRANT_TYPE = "captcha";
	private static final String VERIFICATION_CODE_GRANT_TYPE = "verification_code";
	private static final String EMAIL = "email";
	private static final String SMS = "sms";

	@Override
	@SneakyThrows
	public BladeUserDetails loadUserByUsername(String username) {
		HttpServletRequest request = WebUtil.getRequest();
		// 获取租户ID
		String headerTenant = request.getHeader(TokenUtil.TENANT_HEADER_KEY);
		String paramTenant = request.getParameter(TokenUtil.TENANT_PARAM_KEY);
		if (StringUtil.isAllBlank(headerTenant, paramTenant)) {
			throw new UserDeniedAuthorizationException(TokenUtil.TENANT_NOT_FOUND);
		}
		String tenantId = StringUtils.isBlank(headerTenant) ? paramTenant : headerTenant;

		// 获取租户信息
		R<Tenant> tenant = sysClient.getTenant(tenantId);
		if (tenant.isSuccess()) {
			if (TokenUtil.judgeTenant(tenant.getData())) {
				throw new UserDeniedAuthorizationException(TokenUtil.USER_HAS_NO_TENANT_PERMISSION);
			}
		} else {
			throw new UserDeniedAuthorizationException(TokenUtil.USER_HAS_NO_TENANT);
		}

		// 获取用户类型
		String headerUserType = request.getHeader(TokenUtil.USER_TYPE_HEADER_KEY);
		String paramUserType = request.getParameter(TokenUtil.USER_TYPE_PARAM_KEY);
		if (StringUtil.isAllBlank(headerUserType, paramUserType)) {
//			throw new UserDeniedAuthorizationException(TokenUtil.USER_TYPE_NOT_FOUND);
			headerUserType = TokenUtil.DEFAULT_USER_TYPE;
		}
		String userType = StringUtils.isBlank(headerUserType) ? paramUserType : headerUserType;

		// 远程调用返回数据
//		R<UserInfo> result;
//		// 根据不同用户类型调用对应的接口返回数据，用户可自行拓展
//		if (userType.equals(UserEnum.WEB.getName())) {
//			result = userClient.userInfo(tenantId, username, UserEnum.WEB.getName());
//		} else if (userType.equals(UserEnum.APP.getName())) {
//			result = userClient.userInfo(tenantId, username, UserEnum.APP.getName());
//		} else {
//			result = userClient.userInfo(tenantId, username, UserEnum.OTHER.getName());
//		}
		R<UserInfo> result = null;
		String grant_type = request.getParameter("grant_type");
		String auth_type = request.getParameter("auth_type");
		if (REFRESHTOKEN_GRANT_TYPE.equals(grant_type)) {
			result = userClient.getUserInfoByAccount(tenantId, username, UserEnum.WEB.getName());
		}else {
			if (userType.equals(UserEnum.WEB.getName())) {
				if (VERIFICATION_CODE_GRANT_TYPE.equals(grant_type)) {
					if (SMS.equals(auth_type)) {
						if (TokenUtil.DEFAULT_TENANT_ID.equals(tenantId)) {
							throw new UserDeniedAuthorizationException(TokenUtil.TENANT_NOT_FOUND);
						}
						result = userClient.getUserInfoByMobile(tenantId, username, UserEnum.WEB.getName());
					}else if (EMAIL.equals(auth_type)) {
						result = userClient.getUserInfoByEmail(tenantId, username, UserEnum.WEB.getName());
					}
				}else { // password and captcha
					result = userClient.getUserInfoByAccount(tenantId, username, UserEnum.WEB.getName());
				}
			} else if (userType.equals(UserEnum.APP.getName())) {
				result = userClient.userInfo(tenantId, username, UserEnum.APP.getName());
			} else {
				result = userClient.userInfo(tenantId, username, UserEnum.OTHER.getName());
			}
		}

		// 判断返回信息
		if (result.isSuccess()) {
			UserInfo userInfo = result.getData();
			User user = userInfo.getUser();
			if (user == null || user.getId() == null) {
				throw new UsernameNotFoundException(TokenUtil.USER_NOT_FOUND);
			}
			if (Func.isEmpty(userInfo.getRoles())) {
				throw new UserDeniedAuthorizationException(TokenUtil.USER_HAS_NO_ROLE);
			}
//			return new BladeUserDetails(user.getId(),
//				user.getTenantId(), StringPool.EMPTY, user.getName(), user.getRealName(), user.getDeptId(), user.getPostId(), user.getRoleId(), Func.join(result.getData().getRoles()), Func.toStr(user.getAvatar(), TokenUtil.DEFAULT_AVATAR),
//				username, AuthConstant.ENCRYPT + user.getPassword(), userInfo.getDetail(), true, true, true, true,
//				AuthorityUtils.commaSeparatedStringToAuthorityList(Func.join(result.getData().getRoles())));
			BladeUserDetails userDetails = null;
			if (VERIFICATION_CODE_GRANT_TYPE.equals(grant_type)) {
				// we won't check password when grant_type is verification_code
				userDetails = new BladeUserDetails(user.getAccount(), "{noop}password", true, true, true, true,
					AuthorityUtils.commaSeparatedStringToAuthorityList(Func.join(result.getData().getRoles())));
			} else {
				userDetails = new BladeUserDetails(user.getAccount(), AuthConstant.ENCRYPT + user.getPassword(), true, true, true, true,
					AuthorityUtils.commaSeparatedStringToAuthorityList(Func.join(result.getData().getRoles())));
			}
			userDetails.setUserId(user.getId());
			userDetails.setTenantId(user.getTenantId());
			userDetails.setDeptId(user.getDeptId());
			userDetails.setRoleId(user.getRoleId());
			userDetails.setOauthId(userInfo.getOauthId());
			userDetails.setAccount(user.getAccount());
			userDetails.setMobile(user.getMobile());
			userDetails.setIdentifier(user.getIdentifier());
			userDetails.setName(user.getName());
			userDetails.setRealName(user.getRealName());
			userDetails.setRoleName(Func.join(userInfo.getRoles()));
			userDetails.setAvatar(user.getAvatar());
			userDetails.setSex(user.getSex());

			return userDetails;
		} else {
			throw new UsernameNotFoundException(result.getMsg());
		}
	}

}
