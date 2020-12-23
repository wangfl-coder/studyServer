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
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.service.IExpertBaseService;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
public class ExpertBaseServiceImpl {

	//@Override
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

	//@Override
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
}
