package org.springblade.adata.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.ExpertBase;
import org.springblade.core.tool.node.INode;

import java.util.ArrayList;
import java.util.List;

/**
 * 智库视图类
 *
 * @author wangshan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ExpertBaseVO对象", description = "ExpertBaseVO对象")
public class ExpertBaseVO extends ExpertBase implements INode<ExpertBaseVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 父节点ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long parentId;

	/**
	 * 子孙节点
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<ExpertBaseVO> children;

	/**
	 * 是否有子孙节点
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Boolean hasChildren;

	@Override
	public List<ExpertBaseVO> getChildren() {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}
		return this.children;
	}

	/**
	 * 上级机构
	 */
	private String parentName;

	/**
	 * 机构类型名称
	 */
	private String expertBaseCategoryName;

}
