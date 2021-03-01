package org.springblade.auth.granter;

import org.springblade.auth.constant.AuthConstant;
import org.springblade.auth.utils.TokenUtil;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.redis.cache.BladeRedisCache;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.core.tool.utils.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * VerificationCodeGranter
 *
 * @author oosugi
 */
public class VerificationCodeGranter extends AbstractTokenGranter {

	private static final String GRANT_TYPE = "verification_code";

	private final AuthenticationManager authenticationManager;

	private BladeRedis redisCache;

	public VerificationCodeGranter(AuthenticationManager authenticationManager,
                                   AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, BladeRedis redisCache) {
		this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
		this.redisCache = redisCache;
	}

	protected VerificationCodeGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                      ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
		this.authenticationManager = authenticationManager;
	}

	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		HttpServletRequest request = WebUtil.getRequest();
		// 获取租户ID
		String headerTenant = request.getHeader(TokenUtil.TENANT_HEADER_KEY);
		String paramTenant = request.getParameter(TokenUtil.TENANT_PARAM_KEY);
		if (StringUtil.isAllBlank(headerTenant, paramTenant)) {
			throw new UserDeniedAuthorizationException(TokenUtil.TENANT_NOT_FOUND);
		}
		String tenantId = StringUtils.isBlank(headerTenant) ? paramTenant : headerTenant;

		Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
		// mobile or email
		String principal = parameters.get("username");
		// verification code
		String code = parameters.get("password");
		// token in redis
		String token = parameters.get("token");
		if(StringUtils.isEmpty(code)){
			throw new UserDeniedAuthorizationException(TokenUtil.CAPTCHA_NOT_CORRECT);
		}

//		String value = this.verificationCodeCache.get(VERIFICATION_CODE_CACHE_NAME,token);
		String value = redisCache.get("blade:sms::captcha:" + token);
		if(StringUtils.isEmpty(value)){
			throw new UserDeniedAuthorizationException(TokenUtil.CAPTCHA_NOT_CORRECT);
		}

		if (!code.equalsIgnoreCase(value)) {
//		if (!code.equals(value)) {
			throw new UserDeniedAuthorizationException(TokenUtil.CAPTCHA_NOT_CORRECT);
		}

		Authentication userAuth = new UsernamePasswordAuthenticationToken(principal, "password");
		((AbstractAuthenticationToken) userAuth).setDetails(parameters);
		try {
			userAuth = authenticationManager.authenticate(userAuth);
		}
		catch (AccountStatusException | BadCredentialsException ase) {
			//covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
			throw new InvalidGrantException(ase.getMessage());
		}
		// If the username/password are wrong the spec says we should send 400/invalid grant

		if (userAuth == null || !userAuth.isAuthenticated()) {
			throw new InvalidGrantException("Could not authenticate user: " + principal);
		}

		OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
		return new OAuth2Authentication(storedOAuth2Request, userAuth);
	}
}
