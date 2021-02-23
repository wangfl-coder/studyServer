package org.springblade.adata.magic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springblade.adata.entity.AminerUser;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.constant.SecureConstant;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.WebUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ExpertExtendRequest {
	private static final String AMINER_USER_REQUEST_ATTR = "_AMINER_USER_REQUEST_ATTR_";
	private static String API_PREFIX = "https://apiv2.aminer.cn";
	private static String API_MAGIC = API_PREFIX + "/magic";

	public static final MediaType JSONMediaType = MediaType.parse("application/json;charset=utf-8");


	private static String request(String authorization, String json, String url) throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

		RequestBody body = RequestBody.create(JSONMediaType, json); // new
		Request request = new Request.Builder().url(urlBuilder.build()).post(body).addHeader("Authorization", authorization).build();

		log.info(json);
		try (Response response = new OkHttpClient().newCall(request).execute()) {
			//log.info(response.body().string());
			return response.body().string();
		}
	}

	public static String magic(String authorization, String json) {
		try {
			String res = request(authorization, json, API_MAGIC);
			return res;
		}catch (Exception e) {
			log.error(API_MAGIC, e);
			return null;
		}
	}

	/**
	 * 获取用户信息
	 *
	 * @return BladeUser
	 */
	public static AminerUser getUser() {
		HttpServletRequest request = WebUtil.getRequest();
		if (request == null) {
			return null;
		}
		// 优先从 request 中获取
		Object aminerUser = request.getAttribute(AMINER_USER_REQUEST_ATTR);
		if (aminerUser == null) {
			// 获取请求头客户端信息
			String authorization = Objects.requireNonNull(WebUtil.getRequest()).getHeader(SecureConstant.BASIC_HEADER_KEY);
			if (authorization == null)
				return null;
			aminerUser = getUser(authorization);
			if (aminerUser != null) {
				// 设置到 request 中
				request.setAttribute(AMINER_USER_REQUEST_ATTR, aminerUser);
			}
		}
		return (AminerUser) aminerUser;
	}

	private static AminerUser getUser(String authorization) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		body.put("action", "user.GetMe");
		requestBody.add(body);
		String res = ExpertExtendRequest.magic(authorization, requestBody.toString());
		JSONObject res_obj = JSON.parseObject(res);
		JSONArray array = res_obj.getJSONArray("data");
		JSONObject data = array.getJSONObject(0);
		if (data.getBoolean("error") == null) {
			JSONArray items = data.getJSONArray("items");
			JSONObject item = items.getJSONObject(0);
			String clientId = item.getString("src");
			String userId = item.getString("id");
//			String tenantId = Func.toStr(claims.get(AuthUtil.TENANT_ID));
//			String oauthId = Func.toStr(claims.get(AuthUtil.OAUTH_ID));
//			String deptId = Func.toStrWithEmpty(claims.get(AuthUtil.DEPT_ID), StringPool.MINUS_ONE);
//			String postId = Func.toStrWithEmpty(claims.get(AuthUtil.POST_ID), StringPool.MINUS_ONE);
//			String roleId = Func.toStrWithEmpty(claims.get(AuthUtil.ROLE_ID), StringPool.MINUS_ONE);
			String account = item.getString("email");
//			String roleName = Func.toStr(claims.get(AuthUtil.ROLE_NAME));
			String userName = item.getString("name");
//			String nickName = Func.toStr(claims.get(AuthUtil.NICK_NAME));
//			Kv detail = Kv.create().setAll((Map<? extends String, ?>) claims.get(AuthUtil.DETAIL));
			AminerUser aminerUser = new AminerUser();
			aminerUser.setClientId(clientId);
			aminerUser.setUserId(userId);
//			aminerUser.setTenantId(tenantId);
//			aminerUser.setOauthId(oauthId);
			aminerUser.setAccount(account);
//			aminerUser.setDeptId(deptId);
//			aminerUser.setPostId(postId);
//			aminerUser.setRoleId(roleId);
//			aminerUser.setRoleName(roleName);
			aminerUser.setUserName(userName);
//			aminerUser.setNickName(nickName);
//			aminerUser.setDetail(detail);
			return aminerUser;
		} else {
			return null;
		}
	}
}

