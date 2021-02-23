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
package org.springblade.adata.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.springblade.adata.entity.AminerUser;
import org.springblade.adata.entity.ExpertExtend;
import org.springblade.adata.magic.ExpertExtendRequest;
import org.springblade.adata.mapper.ExpertExtendMapper;
import org.springblade.adata.service.IExpertExtendService;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotEmpty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
public class ExpertExtendServiceImpl<M extends BaseMapper<T>, T extends ExpertExtend> extends ServiceImpl<M, T> implements IExpertExtendService<T> {

//	@Override
//	public String getValue(String paramKey) {
//		Param param = this.getOne(Wrappers.<Param>query().lambda().eq(Param::getParamKey, paramKey));
//		return param.getParamValue();
//	}

	@Override
	public boolean save(T entity) {
		this.resolveEntity(entity);
		return super.save(entity);
	}

	@Override
	public boolean saveBatch(Collection<T> entityList, int batchSize) {
		entityList.forEach(this::resolveEntity);
		return super.saveBatch(entityList, batchSize);
	}

	@Override
	public boolean updateById(T entity) {
		this.resolveEntity(entity);
		return super.updateById(entity);
	}

	@Override
	public boolean updateBatchById(Collection<T> entityList, int batchSize) {
		entityList.forEach(this::resolveEntity);
		return super.updateBatchById(entityList, batchSize);
	}

	@Override
	public boolean saveOrUpdate(T entity) {
		return baseMapper.selectById(entity.getId()) == null ? this.save(entity) : this.updateById(entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteLogic(@NotEmpty List<String> ids) {
		AminerUser user = ExpertExtendRequest.getUser();
		List<T> list = new ArrayList<>();
		ids.forEach(id -> {
			T entity = BeanUtil.newInstance(currentModelClass());
			if (user != null) {
				entity.setUpdateUser(user.getUserId());
			}
			entity.setUpdateTime(DateUtil.now());
			entity.setId(id);
			list.add(entity);
		});
		return super.updateBatchById(list) && super.removeByIds(ids);
	}

	@Override
	public boolean changeStatus(@NotEmpty List<String> ids, Integer status) {
		AminerUser user = ExpertExtendRequest.getUser();
		List<T> list = new ArrayList<>();
		ids.forEach(id -> {
			T entity = BeanUtil.newInstance(currentModelClass());
			if (user != null) {
				entity.setUpdateUser(user.getUserId());
			}
			entity.setUpdateTime(DateUtil.now());
			entity.setId(id);
			entity.setStatus(status);
			list.add(entity);
		});
		return super.updateBatchById(list);
	}

	@SneakyThrows
	private void resolveEntity(T entity) {
		AminerUser user = ExpertExtendRequest.getUser();
		Date now = DateUtil.now();
		if (entity.getId() == null) {
			// 处理新增逻辑
			if (user != null) {
				entity.setCreateUser(user.getUserId());
//				entity.setCreateDept(Func.firstLong(user.getDeptId()).toString());
				entity.setUpdateUser(user.getUserId());
			}
			if (entity.getStatus() == null) {
				entity.setStatus(BladeConstant.DB_STATUS_NORMAL);
			}
			entity.setCreateTime(now);
		} else if (user != null) {
			// 处理修改逻辑
			entity.setUpdateUser(user.getUserId());
		}
		// 处理通用逻辑
		entity.setUpdateTime(now);
		entity.setIsDeleted(BladeConstant.DB_NOT_DELETED);
		// 处理多租户逻辑，若字段值为空，则不进行操作
		Field field = ReflectUtil.getField(entity.getClass(), BladeConstant.DB_TENANT_KEY);
		if (ObjectUtil.isNotEmpty(field)) {
			Method getTenantId = ClassUtil.getMethod(entity.getClass(), BladeConstant.DB_TENANT_KEY_GET_METHOD);
			String tenantId = String.valueOf(getTenantId.invoke(entity));
			if (ObjectUtil.isEmpty(tenantId)) {
				Method setTenantId = ClassUtil.getMethod(entity.getClass(), BladeConstant.DB_TENANT_KEY_SET_METHOD, String.class);
				setTenantId.invoke(entity, (Object) null);
			}
		}
	}

	@SneakyThrows
	private void resolveAminerEntity(T entity) {
		AminerUser user = new AminerUser();
		user.setUserId("6013c4e84c775e411afb71fa"); // uid for pdm
		Date now = DateUtil.now();
		if (entity.getId() == null) {
			// 处理新增逻辑
			if (user != null) {
				entity.setCreateUser(user.getUserId());
//				entity.setCreateDept(Func.firstLong(user.getDeptId()).toString());
				entity.setUpdateUser(user.getUserId());
			}
			if (entity.getStatus() == null) {
				entity.setStatus(BladeConstant.DB_STATUS_NORMAL);
			}
			entity.setCreateTime(now);
		} else if (user != null) {
			// 处理修改逻辑
			entity.setUpdateUser(user.getUserId());
		}
		// 处理通用逻辑
		entity.setUpdateTime(now);
		entity.setIsDeleted(BladeConstant.DB_NOT_DELETED);
		// 处理多租户逻辑，若字段值为空，则不进行操作
		Field field = ReflectUtil.getField(entity.getClass(), BladeConstant.DB_TENANT_KEY);
		if (ObjectUtil.isNotEmpty(field)) {
			Method getTenantId = ClassUtil.getMethod(entity.getClass(), BladeConstant.DB_TENANT_KEY_GET_METHOD);
			String tenantId = String.valueOf(getTenantId.invoke(entity));
			if (ObjectUtil.isEmpty(tenantId)) {
				Method setTenantId = ClassUtil.getMethod(entity.getClass(), BladeConstant.DB_TENANT_KEY_SET_METHOD, String.class);
				setTenantId.invoke(entity, (Object) null);
			}
		}
	}

	@Override
	public boolean saveOrUpdateToAminer(T entity) {
		if (baseMapper.selectById(entity.getId()) == null) {
			this.resolveAminerEntity(entity);
			return super.save(entity);
		}else {
			this.resolveAminerEntity(entity);
			return super.updateById(entity);
		}
	}
}
