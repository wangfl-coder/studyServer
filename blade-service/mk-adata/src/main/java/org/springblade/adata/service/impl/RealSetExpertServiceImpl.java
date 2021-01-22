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
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.mapper.ExpertMapper;
import org.springblade.adata.mapper.RealSetExpertMapper;
import org.springblade.adata.service.IExpertService;
import org.springblade.adata.service.IRealSetExpertService;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.feign.ITemplateClient;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class RealSetExpertServiceImpl extends BaseServiceImpl<RealSetExpertMapper, RealSetExpert> implements IRealSetExpertService {

	private final ITemplateClient iTemplateClient;

	@Override
	public String fetchDetail(String id) {
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
		return res;
	}

	@Override
	public String fetchList(Map<String, Object> params, Query query) {
		String ebId = (String)params.get("ebId");
		if (ebId == null) {
			return ("需要智库Id");
		}
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
		return res;
	}

	@Override
	public Boolean importDetail(String id, Long taskId) {
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
		RealSetExpert expert = new RealSetExpert();
		JSONArray dataArray = resObj.getJSONArray("data");
		JSONObject tempObj = dataArray.getJSONObject(0);
		JSONArray data = tempObj.getJSONArray("data");
		JSONObject d = data.getJSONObject(0);
		String name = d.getString("name");
		String expertId = d.getString("id");
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


//		JSONArray resource_link = resource.getJSONArray("resource_link");
//		JSONObject first = resource_link.getJSONObject(0);
//		JSONObject second = resource_link.getJSONObject(1);
//		String hp = "";
//		String dblp = "";
//		if (first.getString("id").equals("hp")) {
//			hp = first.getString("url");
//		} else {
//			hp = second.getString("url");
//		}
//		if (first.getString("id").equals("dblp")) {
//			dblp = first.getString("url");
//		} else {
//			dblp = second.getString("url");
//		}
		String hp = null;
		String dblp = null;
		String gs = null;
		if (links != null) {
			JSONObject resource = links.getJSONObject("resource");
			if (resource != null) {
				JSONArray resource_link = resource.getJSONArray("resource_link");
				JSONObject first;
				JSONObject second;


				if (resource_link.size() == 1) {
					first = resource_link.getJSONObject(0);
					if (first.getString("id").equals("dblp")) {
						dblp = first.getString("url");
					}
					if (first.getString("id").equals("hp")) {
						hp = first.getString("url");
					}
				} else if (resource_link.size() == 2) {
					first = resource_link.getJSONObject(0);
					second = resource_link.getJSONObject(1);
					if (first.getString("id").equals("dblp")) {
						dblp = first.getString("url");
					} else if(second.getString("id").equals("dblp")){
						dblp = second.getString("url");
					}
					if (first.getString("id").equals("hp")) {
						hp = first.getString("url");
					} else if(second.getString("id").equals("hp")){
						hp = second.getString("url");
					}
				}
			}
			JSONObject google = links.getJSONObject("gs");

			if(google != null) {
				gs = google.getString("url");
			}
		}

		String gender = p.getString("gender");
		String language = p.getString("language");
		String avatar = d.getString("avatar");
		String edu = p.getString("edu");
		String work = p.getString("work");
		String bio = p.getString("bio");
		String bioZh = p.getString("bio_zh");

		expert.setExpertId(expertId);
		expert.setName(name);
		expert.setNameZh(nameZh);
		if (titles != null){
			if (StringUtils.contains(titles, "[")){
				titles = StringUtils.removeEnd(titles, "\"]");
				titles = StringUtils.removeStart(titles, "[\"");
				expert.setTitles(titles);
			} else {
				expert.setTitles("-1");
				titles = StringUtils.removeEnd(titles, "\"");
				titles = StringUtils.removeStart(titles, "\"");
				expert.setTitlesDesc(titles);
			}
		}
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
		return true;
	}

	/**
	 * 每次20个学者请求一次智库
	 * @param ebId
	 * @param taskId
	 * @return
	 */
	public int getExperts(String ebId, Long taskId, int offset, int size) {
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
		parameters.put("offset", offset);
		parameters.put("size", size);
		JSONArray sorts = new JSONArray();
		sorts.add("_id");
		parameters.put("sorts",sorts);


		JSONObject schema = new JSONObject();
		JSONArray expert = new JSONArray();
		expert.add("id");
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
		int total = tempObj.getInteger("total");
		JSONArray experts = tempObj.getJSONArray("items");
		// 存在有的智库中没有学者
		if (total == 0) {
			return 0;
		}
		for (int i = 0; i < experts.size(); i++) {
			String expert_id = experts.getJSONObject(i).getString("id");
			importDetail(expert_id, taskId);
		}
		return total;
	}



	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean importExpertBase(String ebId, Long taskId) {
		if (ebId == null) {
			return false;
		}
		// 首先导入智库下20个学者，并且得到这个智库下一共有多少学者
		int total = getExperts(ebId, taskId, 0, 20);

		// 循环导入剩下的学者
		int number = (total-1) / 20;
		for (int i = 0; i < number ; i++) {
			getExperts(ebId, taskId, (i + 1) * 20, 20);
		}
		return true;
	}




}
