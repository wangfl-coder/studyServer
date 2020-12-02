package org.springblade.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Blog
 *
 * @author KaiLun
 */
//@Data
//@Builder
@TableName("blade_blog")
public class Blog implements Serializable {

	private static final long serialVersionUID = 1L;  //用来表明类的不同版本的兼容性。

	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;
	/**
	 * 标题
	 */
	private String blogTitle;
	/**
	 * 内容
	 */
	private String blogContent;
	/**
	 * 时间
	 */
	private Date blogDate;
	/**
	 * 是否已删除
	 */
	@TableLogic
	private Integer isDeleted;
	/**
	 * 官方主页
	 */
	private String officialHomepage;
	/**
	 *分配的人
	 */
	private Long createUser;
	/**
	 * 标注人更新时间
	 */

	private Date updateTime;
	/**
	 * 目前的状态
	 */
	private Integer status;
	/**
	 * 标注人
	 */
	private Long updateUser;

	public void setId(Long id) {
		this.id = id;
	}

	public void setBlogTitle(String blogTitle) {
		this.blogTitle = blogTitle;
	}

	public void setBlogContent(String blogContent) {
		this.blogContent = blogContent;
	}

	public void setBlogDate(Date blogDate) {
		this.blogDate = blogDate;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setOfficialHomepage(String officialHomepage) {
		this.officialHomepage = officialHomepage;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public Long getId() {
		return id;
	}

	public String getBlogTitle() {
		return blogTitle;
	}

	public String getBlogContent() {
		return blogContent;
	}

	public Date getBlogDate() {
		return blogDate;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public String getOfficialHomepage() {
		return officialHomepage;
	}

	public Long getCreateUser() {
		return createUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public Integer getStatus() {
		return status;
	}

	public Long getUpdateUser() {
		return updateUser;
	}
}
