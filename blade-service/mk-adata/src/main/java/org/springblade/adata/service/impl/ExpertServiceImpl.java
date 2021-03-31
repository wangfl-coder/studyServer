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
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jodd.util.ThreadUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.ExpertExtend;
import org.springblade.adata.entity.ExpertOrigin;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.excel.ExpertExcel;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.mapper.ExpertMapper;
import org.springblade.adata.service.IExpertExtendService;
import org.springblade.adata.service.IExpertOriginService;
import org.springblade.adata.service.IExpertService;
import org.springblade.adata.vo.UserRemarkVO;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.feign.ITemplateClient;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;

import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class ExpertServiceImpl extends BaseServiceImpl<ExpertMapper, Expert> implements IExpertService {

	private final ITemplateClient iTemplateClient;
	private final IUserClient iUserClient;
	private final IExpertExtendService expertExtendService;
	private final IExpertOriginService expertOriginService;
	private final ExpertMapper expertMapper;

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
	public List<String> getExpertsId(Long taskId) {
		List<String> expertIds = expertMapper.getExpertsId(taskId);
		return expertIds;
	}

	@Override
	public Boolean importDetailAndSave(String tenantId, String id, Long taskId) {
		Expert expert = importDetail(tenantId, id, taskId);
		saveOrUpdate(expert);
		ExpertOrigin expertOrigin = Objects.requireNonNull(BeanUtil.copy(expert, ExpertOrigin.class));
		boolean result = expertOriginService.saveOrUpdate(expertOrigin);
		return true;
	}

	/**
	 * 每次20个学者请求一次智库
	 * @param ebId
	 * @param taskId
	 * @return
	 */
	public int getExperts(String tenantId, String ebId, Long taskId, int offset, int size) {
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
			importDetailAndSave(tenantId, expert_id, taskId);
		}
		return total;
	}



	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean importExpertBase(String ebId, Long taskId) {
		if (ebId == null) {
			return false;
		}
		String tenantId = AuthUtil.getTenantId();
		// 首先导入智库下20个学者，并且得到这个智库下一共有多少学者
//		for (int i=0;i<200;i++) {
			int total = getExperts(tenantId, ebId, taskId, 0, 20);
//		}

		// 循环导入剩下的学者
		int number = (total-1) / 20;
		List<Integer> numbers = Stream.iterate(1, n -> n + 1)
			.limit(number)
			.collect(Collectors.toList());

		numbers.parallelStream().forEach(i -> {
			getExperts(tenantId, ebId, taskId, i * 20, 20);
		});
		return true;
	}



	@Override
	public Kv isInfoComplete(Long expertId, Long templateId) {
		Expert expert = getById(expertId);
		Kv kv = Kv.create();
		if (expert == null) {
			kv.set(ProcessConstant.HOMEPAGE_FOUND_KEY, false)
				.set(ProcessConstant.BASICINFO_COMPLETE_KEY, false);
			return kv;
		}
		List<Composition> compositions = (List<Composition>)iTemplateClient.allCompositions(templateId).getData();
		List<String> homepageFields = new ArrayList<>();
		compositions.forEach(composition -> {
			if (1 == composition.getAnnotationType()) {
				String[] fields = composition.getField().split(",");
				homepageFields.addAll(Arrays.asList(fields));
			}
		});
		AtomicInteger homepageExists = new AtomicInteger(0);
		homepageFields.forEach(field -> {
			if (StringUtil.isNotBlank((String)BeanUtil.getProperty(expert, field))){
				homepageExists.getAndIncrement();
			}
		});
		if (homepageExists.get() > 0) {
			kv.set(ProcessConstant.HOMEPAGE_FOUND_KEY, true);
		} else {
			kv.set(ProcessConstant.HOMEPAGE_FOUND_KEY, false);
		}
//		if (StringUtil.isAllBlank(
//			expert.getHomepage(),
//			expert.getHp(),
//			expert.getGs(),
//			expert.getDblp(),
//			expert.getOtherHomepage()
//		)) {
//			kv.set(ProcessConstant.HOMEPAGE_FOUND_KEY, false);
//		} else {
//			kv.set(ProcessConstant.HOMEPAGE_FOUND_KEY, true);
//		}

		List<String> allFields = new ArrayList<>();
		compositions.forEach(composition -> {
			String[] fields = composition.getField().split(",");
			allFields.addAll(Arrays.asList(fields));
			allFields.removeAll(Arrays.asList(""));
		});
		AtomicInteger counter = new AtomicInteger(0);
		allFields.forEach(field -> {
			if (StringUtil.isBlank((String)BeanUtil.getProperty(expert, field))){
				counter.getAndIncrement();
			}
		});
		if (counter.get() > 0) {
			kv.set(ProcessConstant.BASICINFO_COMPLETE_KEY, false);
		} else {
			kv.set(ProcessConstant.BASICINFO_COMPLETE_KEY, true);

		}
		return kv;
	}

	@Override
	public User queryNameById(Long userId) {
		return iUserClient.userInfoById(userId).getData();
	}

	@Override
	public List<UserRemarkVO> userRemark(Long personId) {
		return baseMapper.userRemark(personId);
	}

	@Override
	public List<UserRemarkVO> userInspectionRemark(Long personId) {
		return baseMapper.userInspectionRemark(personId);
	}

	@Override
	public List<ExpertExcel> exportExpert(Wrapper<Expert> queryWrapper) {
		return baseMapper.exportExpert(queryWrapper);
	}

	@Override
	public void importExpert(List<ExpertExcel> data, Boolean isCovered) {
		data.forEach(expertExcel -> {
			Expert expert = Objects.requireNonNull(BeanUtil.copy(expertExcel, Expert.class));
			// 覆盖数据
			if (isCovered) {
				// 查询用户是否存在
				QueryWrapper<Expert> expertQueryWrapper = new QueryWrapper<>();
				expertQueryWrapper.eq("expert_id",expert.getExpertId());
				Expert oldExpert = getOne(expertQueryWrapper);
				if (oldExpert != null && oldExpert.getId() != null) {
					expert.setId(oldExpert.getId());
					updateById(expert);
					return;
				}
			}
			save(expert);
		});
	}

	@Override
	public Expert importDetail(String tenantId, String id, Long taskId) {
		String res = fetchDetail(id);

		JSONObject resObj = JSON.parseObject(res);
		JSONArray array = resObj.getJSONArray("data");
		JSONObject dataRes = array.getJSONObject(0);
		boolean importSuccess = dataRes.getBoolean("succeed");
		while (!importSuccess) {	//确保导入成功
			ThreadUtil.sleep(2000);
			String res2 = fetchDetail(id);
			JSONObject resObj2 = JSON.parseObject(res2);
			JSONArray array2 = resObj2.getJSONArray("data");
			JSONObject dataRes2 = array2.getJSONObject(0);
			importSuccess = dataRes2.getBoolean("succeed");
		}

		Expert expert = new Expert();
		expert.setTenantId(tenantId);
		JSONArray dataArray = resObj.getJSONArray("data");
		JSONObject tempObj = dataArray.getJSONObject(0);
		JSONArray data = tempObj.getJSONArray("data");
		JSONObject d = data.getJSONObject(0);
		String name = d.getString("name");
		String expertId = d.getString("id");
		String nameZh = d.getString("name_zh");
		String titles = d.getString("titles");
		JSONObject p = d.getJSONObject("profile");
		if (p != null) {
			String phone = p.getString("phone");
			String fax = p.getString("fax");
			String email = p.getString("email");
			String affiliation = p.getString("affiliation");
			String affiliation_zh = p.getString("affiliation_zh");
			String address = p.getString("address");
			String homepage = p.getString("homepage");

			String gender = p.getString("gender");
			String language = p.getString("lang");
			String avatar = d.getString("avatar");
			String edu = p.getString("edu");
			String work = p.getString("work");
			String bio = p.getString("bio");
			String bioZh = p.getString("bio_zh");

			expert.setPhone(phone);
			expert.setFax(fax);
			expert.setEmail(email);
			expert.setAffiliation(affiliation);
			expert.setAffiliationZh(affiliation_zh);
			expert.setAddress(address);
			expert.setHomepage(homepage);

			expert.setGender(gender);
			expert.setLanguage(language);
			expert.setAvatar(avatar);
			expert.setEdu(edu);
			expert.setWork(work);
			expert.setBio(bio);
			expert.setBioZh(bioZh);
		}
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
		expert.setHp(hp);
		expert.setGs(gs);
		expert.setDblp(dblp);

		expert.setTaskId(taskId);

		ExpertExtend extend = (ExpertExtend) expertExtendService.getById(expertId);
		if (extend != null) {
			expert.setMag(extend.getMag());
			expert.setOtherHomepage(extend.getOtherHomepage());
		}
		return expert;
	}
}
