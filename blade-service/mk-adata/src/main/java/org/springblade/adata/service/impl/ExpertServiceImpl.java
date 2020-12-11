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

import com.alibaba.fastjson.JSON;
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

	@Override
	public R importDetail(String id, Long taskId) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		JSONArray ids = new JSONArray();
		ids.add(id);
		parameters.put("ids", ids);


		JSONObject schema = new JSONObject();
		JSONArray person = new JSONArray();
		person.add("id");
		person.add("name");
		person.add("name_zh");
		person.add("avatar");
		person.add("links");

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
		person.add(profile_obj);
		schema.put("person", person);

		body.put("action", "personapi.get");
		body.put("parameters", parameters);
		body.put("schema", schema);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());

		JSONObject resObj = JSON.parseObject(res);
		Expert expert = new Expert();
		JSONArray dataArray = resObj.getJSONArray("data");
		JSONObject tempObj = dataArray.getJSONObject(0);
		JSONArray data = tempObj.getJSONArray("data");
		JSONObject d = data.getJSONObject(0);
		String name = d.getString("name");
		String nameZh = d.getString("name_zh");
		String titles = d.getString("titles");
		JSONObject p = d.getJSONObject("profile");
		String phone = p.getString("phone");
		String fax = p.getString("fax");
		String email = p.getString("email");
		String affiliation = p.getString("affiliation");
		String affiliation_zh = p.getString("affiliation_zh");
		String address = p.getString("address");
		String homepage = p.getString("homepage");
		JSONObject links = d.getJSONObject("links");
		JSONObject google = links.getJSONObject("gs");
		JSONObject resource = links.getJSONObject("resource");
		JSONArray resource_link = resource.getJSONArray("resource_link");
		JSONObject first = resource_link.getJSONObject(0);
		JSONObject second = resource_link.getJSONObject(1);
		String hp = "";
		String dblp = "";
		if (first.getString("id").equals("hp")) {
			hp = first.getString("url");
		} else {
			hp = second.getString("url");
		}
		if (first.getString("id").equals("dblp")) {
			dblp = first.getString("url");
		} else {
			dblp = second.getString("url");
		}
		String gs = google.getString("url");
		String gender = p.getString("gender");
		String language = p.getString("language");
		String avatar = d.getString("avatar");
		String edu = p.getString("edu");
		String work = p.getString("work");
		String bio = p.getString("bio");
		String bioZh = p.getString("bio_zh");

		expert.setName(name);
		expert.setNameZh(nameZh);
		expert.setTitles(titles);
		expert.setPhone(phone);
		expert.setFax(fax);
		expert.setEmail(email);
		expert.setAffiliation(affiliation);
		expert.setAffiliationZh(affiliation_zh);
		expert.setAddress(address);
		expert.setHomepage(homepage);
		expert.setHp(hp);
		expert.setGs(gs);
		expert.setDblp(dblp);
		expert.setGender(gender);
		expert.setLanguage(language);
		expert.setAvatar(avatar);
		expert.setEdu(edu);
		expert.setWork(work);
		expert.setBio(bio);
		expert.setBioZh(bioZh);
		expert.setTaskId(taskId);

		saveOrUpdate(expert);
		return R.data(res);
	}
	@Override
	public R<String> importExpertBase(String ebId, Long taskId) {
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

		//解析json,拿到每个学者的id
		JSONObject resObj = JSON.parseObject(res);
		JSONArray dataArray = resObj.getJSONArray("data");
		JSONObject tempObj = dataArray.getJSONObject(0);
		JSONArray experts = tempObj.getJSONArray("items");
		for (int i = 0; i < experts.size(); i++) {
			String expert_id = experts.getJSONObject(i).getString("id");
			importDetail(expert_id, taskId);
		}
		return R.success("导入成功");
	}
}
