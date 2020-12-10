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
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.mapper.ExpertMapper;
import org.springblade.adata.service.IExpertService;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
public class ExpertServiceImpl extends BaseServiceImpl<ExpertMapper, Expert> implements IExpertService {

	@Override
	public R<String> fetchDetail(String id) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		JSONArray ids = new JSONArray();
		ids.add(id);
		parameters.put("ids", ids);


		JSONObject schema = new JSONObject();
		JSONArray expert = new JSONArray();
		expert.add("id");
		expert.add("name");
		expert.add("name_zh");
		expert.add("avatar");
		expert.add("links");

		JSONObject profile_obj = new JSONObject();
		JSONArray profile = new JSONArray();
		profile.add("titles");
		profile.add("phone");
		profile.add("fax");
		profile.add("email");
		profile.add("affiliation");
		profile.add("affiliation_zh");
		profile.add("address");
		profile.add("homepage");
		profile.add("gender");
		profile.add("lang");
		profile.add("edu");
		profile.add("work");
		profile.add("bio");
		profile.add("bio_zh");
		profile.add("position");
		profile.add("position_zh");
		profile_obj.put("profile", profile);
		expert.add(profile_obj);
		schema.put("person", expert);

		body.put("action", "personapi.get");
		body.put("parameters", parameters);
		body.put("schema", schema);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return R.data(res);
	}

	@Override
	public R<String> fetchList(Map<String, Object> params, Query query) {
		String ebId = (String)params.get("ebId");
		if (ebId == null)
			return R.fail("需要智库Id");

		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		JSONObject filters = new JSONObject();
		JSONObject dims = new JSONObject();
		JSONArray eb = new JSONArray();
		eb.add(ebId);
		dims.put("eb", eb);
		filters.put("dims", dims);
		parameters.put("filters", filters);
		parameters.put("searchType", "all");
		parameters.put("offset", 0);
		parameters.put("size", 20);


		JSONObject schema = new JSONObject();
		JSONArray expert = new JSONArray();
		expert.add("id");
		expert.add("name");
		expert.add("name_zh");
		expert.add("avatar");
		expert.add("links");

		JSONObject profile_obj = new JSONObject();
		JSONArray profile = new JSONArray();
		profile.add("titles");
		profile.add("phone");
		profile.add("fax");
		profile.add("email");
		profile.add("affiliation");
		profile.add("affiliation_zh");
		profile.add("address");
		profile.add("homepage");
		profile.add("gender");
		profile.add("lang");
		profile.add("edu");
		profile.add("work");
		profile.add("bio");
		profile.add("bio_zh");
		profile.add("position");
		profile.add("position_zh");
		profile_obj.put("profile", profile);
		expert.add(profile_obj);
		schema.put("person", expert);

		body.put("action", "search.search");
		body.put("parameters", parameters);
		body.put("schema", schema);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return R.data(res);
	}
}
