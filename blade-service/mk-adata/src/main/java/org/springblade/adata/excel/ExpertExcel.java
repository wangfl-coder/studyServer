package org.springblade.adata.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class ExpertExcel implements Serializable {
	private static final long serialVersionUID = 1L;

	@ColumnWidth(40)
	@ExcelProperty("Aminer库中原始的专家id")
	private String expertId;

	@ColumnWidth(20)
	@ExcelProperty("名字")
	private String name;

	@ColumnWidth(20)
	@ExcelProperty("中文名字")
	private String nameZh;

	@ColumnWidth(15)
	@ExcelProperty("职称")
	private String titles;

	@ColumnWidth(15)
	@ExcelProperty("职称描述")
	private String titlesDesc;

	@ColumnWidth(15)
	@ExcelProperty("联系电话")
	private String phone;

	@ColumnWidth(15)
	@ExcelProperty("传真")
	private String fax;

	@ColumnWidth(15)
	@ExcelProperty("电子邮件")
	private String email;

	@ColumnWidth(15)
	@ExcelProperty("英文单位")
	private String affiliation;

	@ColumnWidth(15)
	@ExcelProperty("中文单位")
	private String affiliationZh;

	@ColumnWidth(15)
	@ExcelProperty("地址")
	private String address;

	@ColumnWidth(15)
	@ExcelProperty("个人主页")
	private String homepage;

	@ColumnWidth(20)
	@ExcelProperty("第三方个人主页")
	private String otherHomepage;

	@ColumnWidth(15)
	@ExcelProperty("官方主页")
	private String hp;

	@ColumnWidth(15)
	@ExcelProperty("Google")
	private String gs;

	@ColumnWidth(15)
	@ExcelProperty("dblp")
	private String dblp;

	@ColumnWidth(15)
	@ExcelProperty("性别")
	private String gender;

	@ColumnWidth(15)
	@ExcelProperty("语言")
	private String language;

	@ColumnWidth(15)
	@ExcelProperty("头像")
	private String avatar;

	@ColumnWidth(15)
	@ExcelProperty("教育背景")
	private String edu;

	@ColumnWidth(15)
	@ExcelProperty("工作经历")
	private String work;

	@ColumnWidth(15)
	@ExcelProperty("英文简介")
	private String bio;

	@ColumnWidth(15)
	@ExcelProperty("中文简介")
	private String bioZh;
}
