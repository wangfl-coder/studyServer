package org.springblade.adata.magic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springblade.adata.entity.Expert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExportMagicRequest {

	private String API_PREFIX = "https://apiv2.aminer.cn";
	private String API_MAGIC = API_PREFIX + "/magic";
	private String API_LOGIN = "https://api.aminer.cn/api/auth/signin";

	private OkHttpClient client = new OkHttpClient();
//	private String username = "zhangff_love@sina.com";
//	private String password = "123456";
	private String username = "pdm@aminer.cn";
	private String password = "Shujubu123456";
	private String Authorization = "";
	public static final MediaType JSONMediaType = MediaType.parse("application/json;charset=utf-8");
	/**
	 * Get the singleton {@link MagicRequest}.
	 */
	public static ExportMagicRequest getInstance() {
		return INSTANCE;
	}

	private static final ExportMagicRequest INSTANCE = new ExportMagicRequest();

	private String request(String json, String url) throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

		RequestBody body = RequestBody.create(JSONMediaType, json); // new
		Request request = new Request.Builder().url(urlBuilder.build()).post(body).addHeader("Authorization", Authorization).addHeader("debug", "1").build();

		log.info(json);
		try (Response response = client.newCall(request).execute()) {
			//log.info(response.body().string());
			return response.body().string();
		}
	}

	public String request(String json) {
		try {
			String res = request(json, API_MAGIC);
			JSONObject res_obj = JSON.parseObject(res);
			JSONArray array = res_obj.getJSONArray("data");
			JSONObject data = array.getJSONObject(0);
			if (!data.getBoolean("succeed")) {
				login();
				res = request(json, API_MAGIC);
			} else if (data.getBoolean("succeed") && data.getInteger("total") != null && data.getInteger("total") == 0) {
				login();
				res = request(json, API_MAGIC);
			}
			return res;
		}catch (Exception e) {
			log.error(API_MAGIC, e);
			return null;
		}
	}

	public boolean uploadAvatar(Expert expert){
		List<Map<String, Object>> body = new ArrayList<>();
		JSONArray body2 = new JSONArray();
		HashMap<String, Object> f1 = new HashMap<>();
		f1.put("action","person.UpdateAvatars");
		body.add(f1);
		HashMap<String, Object> f2 = new HashMap<>();
		f2.put("id",expert.getExpertId());
		List<Map<String, Object>> avatars = new ArrayList<>();
		HashMap<String, Object> f3 = new HashMap<>();
		if (expert.getAvatar() != null) {
			f3.put("url", expert.getAvatar());
			avatars.add(f3);
		}else{
			f3.put("url", "");
			avatars.add(f3);
		}
		f2.put("avatars",avatars);
		f1.put("parameters",f2);
		body.add(f1);
		body2.add(f1);
//		String bodyParam = JSON.toJSONString(body);
		String bodyParam = body2.toString();
		String res = request(bodyParam);
		System.out.println(res);
		JSONObject res_obj = JSON.parseObject(res);
		JSONArray array = res_obj.getJSONArray("data");
		JSONObject data = array.getJSONObject(0);
		return data.getBoolean("succeed");
//		return res != null;
	}

	public boolean uploadBasicInfo(Expert expert){
		List<Map<String, Object>> body = new ArrayList<>();
		JSONArray body2 = new JSONArray();
		HashMap<String, Object> f1 = new HashMap<>();
		f1.put("action","person_annotation.UpsertPersonAnnotation");
		body.add(f1);
		HashMap<String, Object> f2 = new HashMap<>();
		f2.put("id",expert.getExpertId());
		f2.put("force_update",true);
		List<Map<String, Object>> fields = new ArrayList<>();
		HashMap<String, Object> name = new HashMap<>();
		HashMap<String, Object> nameZh = new HashMap<>();
		HashMap<String, Object> fax = new HashMap<>();
		HashMap<String, Object> phone = new HashMap<>();
		HashMap<String, Object> language = new HashMap<>();
		HashMap<String, Object> email = new HashMap<>();
		HashMap<String, Object> affiliation = new HashMap<>();
		HashMap<String, Object> affiliationZh = new HashMap<>();
		HashMap<String, Object> titles = new HashMap<>();
		HashMap<String, Object> address = new HashMap<>();
		HashMap<String, Object> gender = new HashMap<>();
		HashMap<String, Object> homepage = new HashMap<>();

		if (expert.getName() != null) {
			name.put("field", "name");
			name.put("value", expert.getName());
			fields.add(name);
		}else{
			name.put("field", "name");
			name.put("value", "");
			fields.add(name);
		}
		if (expert.getNameZh() != null) {
			nameZh.put("field", "name_zh");
			nameZh.put("value", expert.getNameZh());
			fields.add(nameZh);
		}else{
			nameZh.put("field", "name_zh");
			nameZh.put("value", "");
			fields.add(nameZh);
		}
		if (expert.getFax() != null) {
			fax.put("field", "profile.fax");
			fax.put("value", expert.getFax().replace("%_%",";"));
			fields.add(fax);
		}else{
			fax.put("field", "profile.fax");
			fax.put("value", "");
			fields.add(fax);
		}
		if (expert.getPhone() != null) {
			phone.put("field", "profile.phone");
			phone.put("value", expert.getPhone().replace("%_%",";"));
			fields.add(phone);
		}else{
			phone.put("field", "profile.phone");
			phone.put("value", "");
			fields.add(phone);
		}
		if (expert.getLanguage() != null) {
			language.put("field", "language");
			language.put("value", expert.getLanguage());
			fields.add(language);
		}else{
			language.put("field", "language");
			language.put("value", "");
			fields.add(language);
		}
		if (expert.getEmail() != null) {
			email.put("field", "profile.email");
			email.put("value", expert.getEmail().replace("%_%",";"));
			fields.add(email);
		}else{
			email.put("field", "profile.email");
			email.put("value", "");
			fields.add(email);
		}
		if (expert.getAffiliation() != null) {
			affiliation.put("field", "profile.affiliation");
			affiliation.put("value", expert.getAffiliation().replace("%_%","/"));
			fields.add(affiliation);
		}else{
			affiliation.put("field", "profile.affiliation");
			affiliation.put("value", "");
			fields.add(affiliation);
		}
		if (expert.getAffiliationZh() != null) {
			affiliationZh.put("field", "profile.affiliation_zh");
			affiliationZh.put("value", expert.getAffiliationZh().replace("%_%","/"));
			fields.add(affiliationZh);
		}else{
			affiliationZh.put("field", "profile.affiliation_zh");
			affiliationZh.put("value", "");
			fields.add(affiliationZh);
		}
		if (expert.getTitles()!=null && !expert.getTitles().equals("-1")) {
			List<String> title = new ArrayList<>();
			title.add(expert.getTitles());
			titles.put("field", "profile.titles");
			titles.put("value", title);
			fields.add(titles);
		}else if(expert.getTitlesDesc() != null){
			List<String> title = new ArrayList<>();
			title.add(expert.getTitlesDesc());
			titles.put("field", "profile.titles");
			titles.put("value", title);
			fields.add(titles);
		}else{
			List<String> title = new ArrayList<>();
			title.add("");
			titles.put("field", "profile.titles");
			titles.put("value", title);
			fields.add(titles);
		}
		if (expert.getAddress() != null) {
			address.put("field", "profile.address");
			address.put("value", expert.getAddress());
			fields.add(address);
		}else{
			address.put("field", "profile.address");
			address.put("value", "");
			fields.add(address);
		}
		if (expert.getGs() != null) {
			HashMap<String, Object> link = new HashMap<>();
			link.put("field","links");
			HashMap<String, Object> linkValue = new HashMap<>();
			HashMap<String, Object> gs = new HashMap<>();
			gs.put("id","");
			gs.put("type","gs");
			gs.put("url",expert.getGs());
			linkValue.put("gs",gs);
			link.put("value",linkValue);
			fields.add(link);
		}else{
			HashMap<String, Object> link = new HashMap<>();
			link.put("field","links");
			HashMap<String, Object> linkValue = new HashMap<>();
			HashMap<String, Object> gs = new HashMap<>();
			gs.put("id","");
			gs.put("type","gs");
			gs.put("url","");
			linkValue.put("gs",gs);
			link.put("value",linkValue);
			fields.add(link);
		}
		if (expert.getHp() != null && expert.getDblp() != null) {
			HashMap<String, Object> link = new HashMap<>();
			link.put("field","links");
			HashMap<String, Object> linkValue = new HashMap<>();
			HashMap<String, Object> resource = new HashMap<>();
			List<Map<String, Object>> resourceLink = new ArrayList<>();
			HashMap<String, Object> hp = new HashMap<>();
			HashMap<String, Object> dblp = new HashMap<>();
			dblp.put("id","dblp");
			dblp.put("url",expert.getDblp());
			hp.put("id","hp");
			hp.put("url",expert.getHp());
			resourceLink.add(hp);
			resourceLink.add(dblp);
			resource.put("resource_link",resourceLink);
			linkValue.put("resource",resource);
			link.put("value",linkValue);
			fields.add(link);
		}else if (expert.getDblp() != null) {
			HashMap<String, Object> link = new HashMap<>();
			link.put("field","links");
			HashMap<String, Object> linkValue = new HashMap<>();
			HashMap<String, Object> resource = new HashMap<>();
			List<Map<String, Object>> resourceLink = new ArrayList<>();
			HashMap<String, Object> dblp = new HashMap<>();
			dblp.put("id","dblp");
			dblp.put("url",expert.getDblp());
			resourceLink.add(dblp);
			resource.put("resource_link",resourceLink);
			linkValue.put("resource",resource);
			link.put("value",linkValue);
			fields.add(link);
		}else if (expert.getHp() != null){
			HashMap<String, Object> link = new HashMap<>();
			link.put("field","links");
			HashMap<String, Object> linkValue = new HashMap<>();
			HashMap<String, Object> resource = new HashMap<>();
			List<Map<String, Object>> resourceLink = new ArrayList<>();
			HashMap<String, Object> hp = new HashMap<>();
			hp.put("id","hp");
			hp.put("url",expert.getHp());
			resourceLink.add(hp);
			resource.put("resource_link",resourceLink);
			linkValue.put("resource",resource);
			link.put("value",linkValue);
			fields.add(link);
		}
		if (expert.getGender() != null) {
			gender.put("field", "gender");
			gender.put("value", expert.getGender());
			fields.add(gender);
		}else{
			gender.put("field", "gender");
			gender.put("value", "");
			fields.add(gender);
		}
		if (expert.getHomepage() != null) {
			homepage.put("field", "profile.homepage");
			homepage.put("value", expert.getHomepage());
			fields.add(homepage);
		}else{
			homepage.put("field", "profile.homepage");
			homepage.put("value", "");
			fields.add(homepage);
		}
		f2.put("fields",fields);
		f1.put("parameters",f2);
		body.add(f1);
		body2.add(f1);
//		String bodyParam = JSON.toJSONString(body);
		String bodyParam = body2.toString();
		String res = request(bodyParam);
		System.out.println(res);
		JSONObject res_obj = JSON.parseObject(res);
		JSONArray array = res_obj.getJSONArray("data");
		JSONObject data = array.getJSONObject(0);
		return data.getBoolean("succeed");
//		return res != null;
	}

	public boolean uploadWork(Expert expert){
		List<Map<String, Object>> body = new ArrayList<>();
		JSONArray body2 = new JSONArray();
		HashMap<String, Object> f1 = new HashMap<>();
		f1.put("action","person_annotation.UpsertPersonAnnotation");
		body.add(f1);
		HashMap<String, Object> f2 = new HashMap<>();
		f2.put("id",expert.getExpertId());
		f2.put("force_update",true);
		List<Map<String, Object>> fields = new ArrayList<>();
		HashMap<String, Object> f3 = new HashMap<>();
		if (expert.getWork() != null) {
			f3.put("field", "profile.work");
			f3.put("value", expert.getWork());
			fields.add(f3);
		}else{
			f3.put("field", "profile.work");
			f3.put("value", "");
			fields.add(f3);
		}
		f2.put("fields",fields);
		f1.put("parameters",f2);
		body.add(f1);
		body2.add(f1);
//		String bodyParam = JSON.toJSONString(body);
		String bodyParam = body2.toString();
		String res = request(bodyParam);
		System.out.println(res);
		JSONObject res_obj = JSON.parseObject(res);
		JSONArray array = res_obj.getJSONArray("data");
		JSONObject data = array.getJSONObject(0);
		return data.getBoolean("succeed");
//		return res != null;
	}

	public boolean uploadEdu(Expert expert){
		List<Map<String, Object>> body = new ArrayList<>();
		JSONArray body2 = new JSONArray();
		HashMap<String, Object> f1 = new HashMap<>();
		f1.put("action","person_annotation.UpsertPersonAnnotation");
		body.add(f1);
		HashMap<String, Object> f2 = new HashMap<>();
		f2.put("id",expert.getExpertId());
		f2.put("force_update",true);
		List<Map<String, Object>> fields = new ArrayList<>();
		HashMap<String, Object> f3 = new HashMap<>();
		if (expert.getEdu() != null) {
			f3.put("field", "profile.edu");
			f3.put("value", expert.getEdu());
			fields.add(f3);
		}else {
			f3.put("field", "profile.edu");
			f3.put("value", "");
			fields.add(f3);
		}
		f2.put("fields",fields);
		f1.put("parameters",f2);
		body.add(f1);
		body2.add(f1);
//		String bodyParam = JSON.toJSONString(body);
		String bodyParam = body2.toString();
		String res = request(bodyParam);
		System.out.println(res);
		JSONObject res_obj = JSON.parseObject(res);
		JSONArray array = res_obj.getJSONArray("data");
		JSONObject data = array.getJSONObject(0);
		return data.getBoolean("succeed");
//		return res != null;
	}

	public boolean uploadBio(Expert expert){
		List<Map<String, Object>> body = new ArrayList<>();
		JSONArray body2 = new JSONArray();
		HashMap<String, Object> f1 = new HashMap<>();
		f1.put("action","person_annotation.UpsertPersonAnnotation");
		body.add(f1);
		HashMap<String, Object> f2 = new HashMap<>();
		f2.put("id",expert.getExpertId());
		f2.put("force_update",true);
		List<Map<String, Object>> fields = new ArrayList<>();
		HashMap<String, Object> bio = new HashMap<>();
		HashMap<String, Object> bioZh = new HashMap<>();
		if (expert.getBio() != null) {
			bio.put("field", "profile.bio");
			bio.put("value", expert.getBio());
			fields.add(bio);
		}else {
			bio.put("field", "profile.bio");
			bio.put("value", "");
			fields.add(bio);
		}
		if (expert.getBioZh() != null) {
			bioZh.put("field", "profile.bio_zh");
			bioZh.put("value", expert.getBioZh());
			fields.add(bioZh);
		}else {
			bioZh.put("field", "profile.bio_zh");
			bioZh.put("value", "");
			fields.add(bioZh);
		}
		f2.put("fields",fields);
		f1.put("parameters",f2);
		body.add(f1);
		body2.add(f1);
//		String bodyParam = JSON.toJSONString(body);
		String bodyParam = body2.toString();
		String res = request(bodyParam);
		System.out.println(res);
		JSONObject res_obj = JSON.parseObject(res);
		JSONArray array = res_obj.getJSONArray("data");
		JSONObject data = array.getJSONObject(0);
		return data.getBoolean("succeed");
//		return res != null;
	}

	public boolean uploadRemark(Expert expert,String userRealName,List<Map<String, Object>> userCommentList){
		List<Map<String, Object>> body = new ArrayList<>();
		JSONArray body2 = new JSONArray();
		HashMap<String, Object> f1 = new HashMap<>();
		f1.put("action","person.SetNotesToPerson");
		body.add(f1);
		HashMap<String, Object> f2 = new HashMap<>();
		f2.put("id",expert.getExpertId());
		StringBuilder remark = new StringBuilder();
		for(Map<String, Object> userComment:userCommentList){
			String comment=userComment.get("user")+"："+userComment.get("comment")+"\n";
			remark.append(comment);
		}
		if(expert.getRemark()!=null){
			remark.append(userRealName).append("：").append(expert.getRemark());
		}
		f2.put("notes_other", remark.toString());
		f2.put("op","1");
		f1.put("parameters",f2);
		body.add(f1);
		body2.add(f1);
//		String bodyParam = JSON.toJSONString(body);
		String bodyParam = body2.toString();
		String res = request(bodyParam);
		System.out.println(res);
		JSONObject res_obj = JSON.parseObject(res);
		JSONArray array = res_obj.getJSONArray("data");
		JSONObject data = array.getJSONObject(0);
		return data.getBoolean("succeed");
//		return res != null;
	}

	public String login() {
		try {
			JSONObject login_body = new JSONObject();
			login_body.put("email", username);
			login_body.put("password", password);
			login_body.put("persist", true);
			login_body.put("src", "aminer");
			String res = request(login_body.toString(), API_LOGIN);
			JSONObject res_obj = JSON.parseObject(res);
			if (res_obj.getBoolean("status")) {
				this.Authorization = res_obj.getString("token");
			}
			return res;
		}catch (Exception e) {
			log.error(API_LOGIN, e);
			return null;
		}
	}

}

