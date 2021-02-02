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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springblade.adata.cache.ExpertBaseCache;
import org.springblade.adata.entity.ExpertBase;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.mapper.ExpertBaseMapper;
import org.springblade.adata.service.IExpertBaseService;
import org.springblade.adata.vo.ExpertBaseVO;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.node.ForestNodeMerger;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
public class ExpertBaseServiceImpl extends ServiceImpl<ExpertBaseMapper, ExpertBase> implements IExpertBaseService {

	private static final String TENANT_ID = "tenantId";
	private static final String PARENT_ID = "parentId";

	@Override
	public R<String> detail(String id) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		JSONArray ids = new JSONArray();
		ids.add(id);
		JSONArray switches = new JSONArray();
		switches.add("master");
		parameters.put("ids", ids);
		parameters.put("switches", switches);
		parameters.put("offset", 0);
		parameters.put("size", 1000);

		JSONObject schema = new JSONObject();
		JSONArray expertbase = new JSONArray();
		expertbase.add("name");
		expertbase.add("name_zh");
		expertbase.add("logo");
		expertbase.add("type");
		expertbase.add("stats");
		expertbase.add("is_deleted");
		expertbase.add("parents");
		expertbase.add("is_public");
		expertbase.add("price");
		expertbase.add("report_link");
		expertbase.add("order");
		expertbase.add("desc");
		expertbase.add("desc_zh");
		expertbase.add("created_time");
		expertbase.add("updated_time");
		expertbase.add("creator");
		expertbase.add("system");
		expertbase.add("related_venues");
		expertbase.add("labels");
		schema.put("expertbase", expertbase);

		body.put("action", "search.search");
		body.put("parameters", parameters);
		body.put("schema", schema);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return R.data(res);
	}

	@Override
	public R<String> list(Map<String, Object> params, Query query) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		JSONObject filters = new JSONObject();
		filters.put("system", "aminer");
		JSONArray sorts = new JSONArray();
		sorts.add("created_time");
		parameters.put("filters", filters);
		parameters.put("sorts", sorts);
		parameters.put("offset", 0);
		parameters.put("size", 1000);
		parameters.put("asc", -1);

		JSONObject schema = new JSONObject();
		JSONArray expertbase = new JSONArray();
		expertbase.add("name");
		expertbase.add("name_zh");
		expertbase.add("logo");
		expertbase.add("order");
		expertbase.add("type");
		expertbase.add("stats");
		expertbase.add("parents");
		expertbase.add("is_deleted");
		expertbase.add("is_public");
		expertbase.add("price");
		expertbase.add("report_link");
		expertbase.add("order");
		expertbase.add("type");
		schema.put("expertbase", expertbase);

		body.put("action", "expertbase.search.myeb");
		body.put("parameters", parameters);
		body.put("schema", schema);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return R.data(res);
	}

	@Override
	public List<ExpertBaseVO> lazyList(String tenantId, Long parentId, Map<String, Object> param) {
		// 设置租户ID
		if (AuthUtil.isAdministrator()) {
			tenantId = StringPool.EMPTY;
		}
		String paramTenantId = Func.toStr(param.get(TENANT_ID));
		if (Func.isNotEmpty(paramTenantId) && AuthUtil.isAdministrator()) {
			tenantId = paramTenantId;
		}
		// 判断点击搜索但是没有查询条件的情况
		if (Func.isEmpty(param.get(PARENT_ID)) && param.size() == 1) {
			parentId = 0L;
		}
		// 判断数据权限控制,非超管角色只可看到本级及以下数据
		// TODO wangshan 暂时只有超管能看
		if (Func.toLong(parentId) == 0L && !AuthUtil.isAdministrator()) {
//			Long expertBaseId = Func.firstLong(AuthUtil.getExpertBaseId());
//			ExpertBase expertBase = ExpertBaseCache.getExpertBase(expertBaseId);
//			if (expertBase.getParentId() != 0) {
//				parentId = expertBase.getParentId();
//			}
		}
		// 判断点击搜索带有查询条件的情况
		if (Func.isEmpty(param.get(PARENT_ID)) && param.size() > 1 && Func.toLong(parentId) == 0L) {
			parentId = null;
		}
		return baseMapper.lazyList(tenantId, parentId, param);
	}


	@Override
	public List<ExpertBaseVO> tree(String tenantId) {
		return ForestNodeMerger.merge(baseMapper.tree(tenantId));
	}

	@Override
	public List<ExpertBaseVO> lazyTree(String tenantId, Long parentId) {
		if (AuthUtil.isAdministrator()) {
			tenantId = StringPool.EMPTY;
		}
		return ForestNodeMerger.merge(baseMapper.lazyTree(tenantId, parentId));
	}

	@Override
	public String getExpertBaseIds(String tenantId, String expertBaseNames) {
		List<ExpertBase> expertBaseList = baseMapper.selectList(Wrappers.<ExpertBase>query().lambda().eq(ExpertBase::getTenantId, tenantId).in(ExpertBase::getExpertBaseName, Func.toStrList(expertBaseNames)));
		if (expertBaseList != null && expertBaseList.size() > 0) {
			return expertBaseList.stream().map(expertBase -> Func.toStr(expertBase.getId())).distinct().collect(Collectors.joining(","));
		}
		return null;
	}

	@Override
	public List<String> getExpertBaseNames(String expertBaseIds) {
		return baseMapper.getExpertBaseNames(Func.toLongArray(expertBaseIds));
	}

	@Override
	public List<ExpertBase> getExpertBaseChild(Long expertBaseId) {
		return baseMapper.selectList(Wrappers.<ExpertBase>query().lambda().like(ExpertBase::getAncestors, expertBaseId));
	}

	@Override
	public boolean removeExpertBase(String ids) {
		Integer cnt = baseMapper.selectCount(Wrappers.<ExpertBase>query().lambda().in(ExpertBase::getParentId, Func.toLongList(ids)));
		if (cnt > 0) {
			throw new ServiceException("请先删除子节点!");
		}
		return removeByIds(Func.toLongList(ids));
	}

	@Override
	public boolean submit(ExpertBase expertBase) {
		if (Func.isEmpty(expertBase.getParentId())) {
			expertBase.setTenantId(AuthUtil.getTenantId());
			expertBase.setParentId(BladeConstant.TOP_PARENT_ID);
			expertBase.setAncestors(String.valueOf(BladeConstant.TOP_PARENT_ID));
		}
		if (expertBase.getParentId() > 0) {
			ExpertBase parent = getById(expertBase.getParentId());
			if (expertBase.getParentId().equals(expertBase.getId())) {
				throw new ServiceException("父节点不可选择自身!");
			}
			expertBase.setTenantId(parent.getTenantId());
			String ancestors = parent.getAncestors() + StringPool.COMMA + expertBase.getParentId();
			expertBase.setAncestors(ancestors);
		}
		expertBase.setIsDeleted(BladeConstant.DB_NOT_DELETED);
		return saveOrUpdate(expertBase);
	}
}
