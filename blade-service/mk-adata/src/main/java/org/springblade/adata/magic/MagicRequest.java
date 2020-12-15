package org.springblade.adata.magic;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class MagicRequest {
	private String API_PREFIX = "https://apiv2.aminer.cn";
	private String API_MAGIC = API_PREFIX + "/magic";
	private String API_LOGIN = "https://api.aminer.cn/api/auth/signin";

	private OkHttpClient client = new OkHttpClient();
	private String username = "zhangff_love@sina.com";
	private String password = "123456";
	private String Authorization = "";
	public static final MediaType JSONMediaType = MediaType.parse("application/json;charset=utf-8");
	/**
	 * Get the singleton {@link MagicRequest}.
	 */
	public static MagicRequest getInstance() {
		return INSTANCE;
	}

	private static final MagicRequest INSTANCE = new MagicRequest();

	private String request(String json, String url) throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

		RequestBody body = RequestBody.create(JSONMediaType, json); // new
		Request request = new Request.Builder().url(urlBuilder.build()).post(body).addHeader("Authorization", Authorization).build();

		log.info(json);
		try (Response response = client.newCall(request).execute()) {
			//log.info(response.body().string());
			return response.body().string();
		}
	}

	private String upload(String action, String parameters, MultipartFile file) throws IOException {
		ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()){
			@Override
			public String getFilename(){
				return file.getOriginalFilename();
			}
		};
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("action", 		  action);
		body.add("parameters", parameters);
		body.add("file",	  fileAsResource);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
		headers.add("Authorization", Authorization);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> res = restTemplate.postForEntity(API_MAGIC, requestEntity, String.class);
		return res.getBody();
	}

	public String magic(String json) {
		try {
			String res = request(json, API_MAGIC);
			JSONObject res_obj = JSON.parseObject(res);
			JSONArray array = res_obj.getJSONArray("data");
			JSONObject data = array.getJSONObject(0);
			if (!data.getBoolean("succeed")) {
				// "action": "search.search" API returns {"data":[{"succeed":false,"error":["server encounter internal error. "]}]} when Authorization failed,
				// from which we can't determine the error, so we login again for all cases.
//				if (403 == data.getInteger("err_code")) {
//					login();
//					res = request(json, API_MAGIC);
//				}
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

	public String uploadAvatar(String action, String parameters, MultipartFile file) {
		try {
			String res = upload(action, parameters, file);
//			String res = request(json, API_MAGIC);
			JSONObject res_obj = JSON.parseObject(res);
			JSONArray array = res_obj.getJSONArray("data");
			JSONObject data = array.getJSONObject(0);
			if (!data.getBoolean("succeed")) {
				// "action": "search.search" API returns {"data":[{"succeed":false,"error":["server encounter internal error. "]}]} when Authorization failed,
				// from which we can't determine the error, so we login again for all cases.
//				if (403 == data.getInteger("err_code")) {
//					login();
//					res = request(json, API_MAGIC);
//				}
				login();
				res = upload(action, parameters, file);
			}
//			} else if (data.getBoolean("succeed") && data.getInteger("total") != null && data.getInteger("total") == 0) {
//				login();
//				res = request(json, API_MAGIC);
//			}
			return res;
		}catch (Exception e) {
			log.error(API_MAGIC, e);
			return null;
		}
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

