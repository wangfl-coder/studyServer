package org.springblade.adata.entity;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.tool.support.Kv;

import java.io.Serializable;

/**
 * 用户实体
 *
 * @author Chill
 */
@Data
public class AminerUser implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 客户端id
	 */
	@ApiModelProperty(hidden = true)
	private String clientId;

	/**
	 * 用户id
	 */
	@ApiModelProperty(hidden = true)
	private String userId;
	/**
	 * 账号
	 */
	@ApiModelProperty(hidden = true)
	private String account;
	/**
	 * 用户名
	 */
	@ApiModelProperty(hidden = true)
	private String userName;
	/**
	 * 昵称
	 */
	@ApiModelProperty(hidden = true)
	private String nickName;
	/**
	 * 租户ID
	 */
	@ApiModelProperty(hidden = true)
	private String tenantId;
	/**
	 * 第三方认证ID
	 */
	@ApiModelProperty(hidden = true)
	private String oauthId;
	/**
	 * 部门id
	 */
	@ApiModelProperty(hidden = true)
	private String deptId;
	/**
	 * 岗位id
	 */
	@ApiModelProperty(hidden = true)
	private String postId;
	/**
	 * 角色id
	 */
	@ApiModelProperty(hidden = true)
	private String roleId;
	/**
	 * 角色名
	 */
	@ApiModelProperty(hidden = true)
	private String roleName;
	/**
	 * 用户详情
	 */
	@ApiModelProperty(hidden = true)
	private Kv detail;

}
