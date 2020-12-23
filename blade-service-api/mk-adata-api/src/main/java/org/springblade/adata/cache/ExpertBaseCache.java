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
package org.springblade.adata.cache;

import org.springblade.adata.entity.ExpertBase;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.adata.feign.IExpertBaseClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

/**
 * 系统缓存
 *
 * @author Chill
 */
public class ExpertBaseCache {
	private static final String EXPERTBASE_ID = "dept:id:";
	private static final String EXPERTBASE_NAME = "dept:name:";
	private static final String EXPERTBASE_NAME_ID = "deptName:id:";
	private static final String EXPERTBASE_NAMES_ID = "deptNames:id:";
	private static final String EXPERTBASE_CHILD_ID = "deptChild:id:";
	private static final String EXPERTBASE_CHILDIDS_ID = "deptChildIds:id:";

	private static IExpertBaseClient expertBaseClient;

	private static IExpertBaseClient getExpertBaseClient() {
		if (expertBaseClient == null) {
			expertBaseClient = SpringUtil.getBean(IExpertBaseClient.class);
		}
		return expertBaseClient;
	}

	/**
	 * 获取部门
	 *
	 * @param id 主键
	 * @return
	 */
	public static ExpertBase getExpertBase(Long id) {
		return CacheUtil.get(SYS_CACHE, EXPERTBASE_ID, id, () -> {
			R<ExpertBase> result = getExpertBaseClient().getExpertBase(id);
			return result.getData();
		});
	}

	/**
	 * 获取部门id
	 *
	 * @param tenantId  租户id
	 * @param deptNames 部门名
	 * @return
	 */
	public static String getExpertBaseIds(String tenantId, String deptNames) {
		return CacheUtil.get(SYS_CACHE, EXPERTBASE_NAME, tenantId + StringPool.DASH + deptNames, () -> {
			R<String> result = getExpertBaseClient().getExpertBaseIds(tenantId, deptNames);
			return result.getData();
		});
	}

	/**
	 * 获取部门名
	 *
	 * @param id 主键
	 * @return 部门名
	 */
	public static String getExpertBaseName(Long id) {
		return CacheUtil.get(SYS_CACHE, EXPERTBASE_NAME_ID, id, () -> {
			R<String> result = getExpertBaseClient().getExpertBaseName(id);
			return result.getData();
		});
	}


	/**
	 * 获取部门名集合
	 *
	 * @param deptIds 主键集合
	 * @return 部门名
	 */
	public static List<String> getExpertBaseNames(String deptIds) {
		return CacheUtil.get(SYS_CACHE, EXPERTBASE_NAMES_ID, deptIds, () -> {
			R<List<String>> result = getExpertBaseClient().getExpertBaseNames(deptIds);
			return result.getData();
		});
	}

	/**
	 * 获取子部门集合
	 *
	 * @param deptId 主键
	 * @return 子部门
	 */
	public static List<ExpertBase> getExpertBaseChild(Long deptId) {
		return CacheUtil.get(SYS_CACHE, EXPERTBASE_CHILD_ID, deptId, () -> {
			R<List<ExpertBase>> result = getExpertBaseClient().getExpertBaseChild(deptId);
			return result.getData();
		});
	}

	/**
	 * 获取子部门ID集合
	 *
	 * @param deptId 主键
	 * @return 子部门ID
	 */
	public static List<Long> getExpertBaseChildIds(Long deptId) {
		if (deptId == null) {
			return null;
		}
		List<Long> deptIdList = CacheUtil.get(SYS_CACHE, EXPERTBASE_CHILDIDS_ID, deptId, List.class);
		if (deptIdList == null) {
			deptIdList = new ArrayList<>();
			List<ExpertBase> deptChild = getExpertBaseChild(deptId);
			if (deptChild != null) {
				List<Long> collect = deptChild.stream().map(ExpertBase::getId).collect(Collectors.toList());
				deptIdList.addAll(collect);
			}
			deptIdList.add(deptId);
			CacheUtil.put(SYS_CACHE, EXPERTBASE_CHILDIDS_ID, deptId, deptIdList);
		}
		return deptIdList;
	}
}
