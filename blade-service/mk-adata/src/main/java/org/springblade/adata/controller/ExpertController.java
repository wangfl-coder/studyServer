/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright expert,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  expert, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.adata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import jodd.util.ThreadUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.ExpertExtend;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.excel.ExpertExcel;
import org.springblade.adata.excel.ExpertImporter;
import org.springblade.adata.magic.ExportMagicRequest;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.mapper.ExpertMapper;
import org.springblade.adata.service.IExpertExtendService;
import org.springblade.adata.service.IExpertService;
import org.springblade.adata.service.IRealSetExpertService;
import org.springblade.adata.vo.ExpertVO;
import org.springblade.adata.vo.RealSetExpertVO;
import org.springblade.adata.vo.UserRemarkVO;
import org.springblade.adata.wrapper.ExpertWrapper;
import org.springblade.adata.wrapper.RealSetExpertWrapper;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.oss.model.BladeFile;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.feign.IFlowEngineClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.entity.UserOauth;
import org.springblade.task.entity.Task;
import org.springblade.task.enums.TaskStatusEnum;
import org.springblade.task.feign.ITaskClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springblade.core.cache.constant.CacheConstant.PARAM_CACHE;



/**
 * 控制器
 *
 * @author Chill
 */
@Slf4j
@NonDS
@RestController
@RequestMapping("/expert")
@AllArgsConstructor
@Api(value = "学者接口", tags = "学者接口")
public class ExpertController extends BladeController {

	private final IExpertService expertService;
	private final IExpertExtendService expertExtendService;
	private final IRealSetExpertService realSetExpertService;
	private final IFlowEngineClient flowEngineClient;
	private final ITaskClient taskClient;
	private final ExpertMapper expertMapper;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入expert")
	public R<ExpertVO> detail(Expert expert) {
		Expert detail = expertService.getOne(Condition.getQueryWrapper(expert));
		return R.data(ExpertWrapper.build().entityVO(detail));
	}

	/**
	 * 真集学者详情
	 */
	@GetMapping("/detail-realset")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入expert")
	public R<RealSetExpertVO> detail(RealSetExpert expert) {
		RealSetExpert detail = realSetExpertService.getOne(Condition.getQueryWrapper(expert));
		return R.data(RealSetExpertWrapper.build().entityVO(detail));
	}

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "category", value = "公告类型", paramType = "query", dataType = "integer"),
		@ApiImplicitParam(name = "title", value = "公告标题", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入expert")
	public R<IPage<ExpertVO>> list(@ApiIgnore @RequestParam Map<String, Object> expert, Query query) {
		IPage<Expert> pages = expertService.page(Condition.getPage(query), Condition.getQueryWrapper(expert, Expert.class));
		return R.data(ExpertWrapper.build().pageVO(pages));
	}

//	/**
//	 * 多表联合查询自定义分页
//	 */
//	@GetMapping("/page")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "category", value = "公告类型", paramType = "query", dataType = "integer"),
//		@ApiImplicitParam(name = "title", value = "公告标题", paramType = "query", dataType = "string")
//	})
//	@ApiOperationSupport(order = 3)
//	@ApiOperation(value = "分页", notes = "传入expert")
//	public R<IPage<ExpertVO>> page(@ApiIgnore ExpertVO expert, Query query) {
//		IPage<ExpertVO> pages = expertService.selectNoticePage(Condition.getPage(query), expert);
//		return R.data(pages);
//	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入expert")
	public R save(@RequestBody Expert expert) {
		return R.status(expertService.save(expert));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入expert")
	public R update(@RequestBody Expert expert) {
		return R.status(expertService.updateById(expert));
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入expert")
	public R submit(@RequestBody Expert expert) {
		return R.status(expertService.saveOrUpdate(expert));
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入expert")
	public R remove(@ApiParam(value = "主键集合") @RequestParam String ids) {
		boolean temp = expertService.deleteLogic(Func.toLongList(ids));
		return R.status(temp);
	}

	/**
	 * 详情
	 */
	@GetMapping("/fetch-detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入学者id")
	public R<String> fetchDetail(String id) {
		return R.data(expertService.fetchDetail(id));
	}

	/**
	 * 列表
	 */
	@GetMapping("/fetch-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "ebId", value = "智库Id", paramType = "query", dataType = "string"),
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入expert")
	public R<String> fetchList(@ApiIgnore @RequestParam Map<String, Object> params, Query query) {
		return R.data(expertService.fetchList(params, query));
	}

	/**
	 * 导入
	 */
	@PostMapping("/import")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "导入", notes = "传入学者id,任务id")
	public R importDetail(String id, Long taskId) {
		String tenantId = AuthUtil.getTenantId();
		return R.data(expertService.importDetail(tenantId, id, taskId));
	}

	/**
	 * 导入智库下所有学者
	 */
	@PostMapping("/expert_base_import")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "导入", notes = "传入智库id,任务id")
	public R importExpertBase(String id, Long taskId) {
		return R.data(expertService.importExpertBase(id,taskId));
	}

	/**
	 * 上传学者头像
	 *
	 * @param action 	 学者Id
	 * @param parameters 学者Id
	 * @param file     	 文件
	 * @return ObjectStat
	 */
	@PostMapping("/put-avatar-by-id")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "上传学者头像", notes = "上传学者头像")
	public R putFile(@RequestParam String action, @RequestParam String parameters, @RequestParam MultipartFile file) {
		if (file.isEmpty()) {
			return R.fail("参数为空");
		}
		String res = MagicRequest.getInstance().uploadAvatar(action, parameters, file);
		return R.data(res);
	}

	/**
	 * 标注数据生效到aminer
	 */
	@GetMapping("/export-experts-aminer")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "标注数据生效到aminer", notes = "标注数据生效到aminer")
	public R exportExperts(Long taskId){
		List<Expert> experts = expertMapper.queryExportExperts(taskId);

//		ArrayList<Expert> experts = new ArrayList<>();
//		Expert expert1 = new Expert();
//		expert1.setWork("中南大学湘雅三医院 博士生导师，一级主任医师");
//		expert1.setEdu("毕业于湖南医科大学");
//		expert1.setBio("dalao");
//		expert1.setBioZh("医学博士，博士生导师，一级主任医师，中南大学首届湘雅名医，全国医德标兵，中国最美女医师，湖南省十大同心人物，享受国务院政府特殊津贴。主要研究方向妇科肿瘤及妇科微无创技术。擅长达芬奇机器人手术。现担任国际微创与无创理事会理事，世界华人妇产科医师协会常务委员,中国医师协会微无创专业委员会副主任委员，中国医师协会微无创医学专业委员会第一届子宫肌瘤专业委员会（学组）主任委员，中国医师协会妇产科医师分会第三届委员会常务委员，湖南省医师协会第一届妇产科医师分会会长，湖南省医学会妇产科专业委员会副主任委员等国际国内学术职务20余项。担任《中南大学学报(医学版)》等7部杂志编委及审稿专家。近年主持及参与国家级课题5项,主持省自然科学基金重点项目、省自然科学基金及科技计划重点项目3项，省厅级一般科研课题20余项，国家专利20余项；在国内外期刊发表论文200余篇，其中SCI论文40余篇；主编/副主编专著10部、参编11部；获省级科技进步奖及省医学科学技术奖6项，省级教学成果奖1项。她是湖南省最早开展妇科腔镜技术的专家之一；她所领导的妇产科目前是湖南省唯一的宫内疾病微创诊治临床研究中心。她于2015年10月15日完成湖南省首例达芬奇机器人手术，在短短两个多月时间即创下2015年度全国妇科达芬奇机器人手术量单月第一的佳绩！也是国内首个妇科机器人手术单月完成达40例的妇产科专家。她2016年度及2017年度达芬奇机器人手术量居全国第一。她是在最短时间内完成手术种类最多的妇科术者之一，且实现了单人单日手术达7台的纪录。");
//		expert1.setPhone("234567");
//		expert1.setName("Min Xue");
//		expert1.setNameZh("李树春");
//		expert1.setExpertId("542a516fdabfae86fd95158b");
//		expert1.setTitles("0");
//		expert1.setTitlesDesc("董事长");
//		expert1.setPhone("0731-88638888%_%88618577%_%88618120");
//		expert1.setFax("0731—88921910");
//		expert1.setEmail("jasonlsc@126.com");
//		expert1.setAffiliation("The Third Xiangya Hospital of Central South University");
//		expert1.setAffiliationZh("中南大学湘雅三医院");
//		expert1.setHp("http://www.xy3yy.com/zjfc/fk2019/15670.html");
//		expert1.setHomepage("http://cpa.cqu.edu.cn/info/1071/3475.htm");
//		expert1.setGs("http://cpa.cqu.edu.cn/info/1071/3475.htm");
//		expert1.setDblp("http://cpa.cqu.edu.cn/info/1071/3475.htm");
//		expert1.setGender("female");
//		expert1.setLanguage("chinese");
//		expert1.setId(1341597796091850753L);
//		expert1.setAddress("中央民族大学中国少数民族传统医学中心１号楼301室");
//		expert1.setAvatar("https://static.aminer.cn/upload/avatar/1306/1243/1303/542a516fdabfae86fd95158b_0.jpg");
//		expert1.setRemark("没有主页");
//		expert1.setUpdateUser(1341216104114147329L);
//		experts.add(expert1);

//		for(Expert expert:experts){
		ExportMagicRequest request = new ExportMagicRequest();
		AtomicInteger count = new AtomicInteger(0);
		AtomicInteger errcnt = new AtomicInteger(0);
//		experts.parallelStream().forEach(expert -> {

		for(Expert expert: experts) {
			count.getAndIncrement();
			log.info("upload expert: "+count);
			log.info("error count: "+errcnt);
			if(expert.getExpertId()!=null){
				boolean avatar = request.uploadAvatar(expert);
				while(!avatar) {
					errcnt.getAndIncrement();
					log.error("导出头像失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					avatar = request.uploadAvatar(expert);
				}
				boolean bi = request.uploadBasicInfo(expert);
				while(!bi) {
					errcnt.getAndIncrement();
					log.error("导出基本信息失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					bi = request.uploadBasicInfo(expert);
				}
				boolean work = request.uploadWork(expert);
				while(!work) {
					errcnt.getAndIncrement();
					log.error("导出工作经历失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					work = request.uploadWork(expert);
				}
				boolean edu = request.uploadEdu(expert);
				while(!edu) {
					errcnt.getAndIncrement();
					log.error("导出教育经历失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					edu = request.uploadEdu(expert);
				}
				boolean bio = request.uploadBio(expert);
				while(!bio) {
					errcnt.getAndIncrement();
					log.error("导出个人简介失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					bio = request.uploadBio(expert);
				}
				String userRealName;
				if(expert.getRemark()!=null){
					userRealName = expertService.queryNameById(expert.getUpdateUser()).getRealName();
				}else{
					userRealName = "任意值";
				}
				List<UserRemarkVO> userRemarkVOS = expertService.userRemark(expert.getId());
				List<UserRemarkVO> userInspectionRemarkVOS = expertService.userInspectionRemark(expert.getId());
				if(userInspectionRemarkVOS.size()!=0){
					userRemarkVOS.addAll(userInspectionRemarkVOS);
				}
				List<Map<String, Object>> userCommentList = new ArrayList<>();
				for(UserRemarkVO userRemarkVO:userRemarkVOS){
					if(userRemarkVO.getProcessInstanceId()!=null){
						R<List<BladeFlow>> listR = flowEngineClient.historyFlow(userRemarkVO.getProcessInstanceId(), "start", "end");
						if(listR.getData()!=null) {
							for (BladeFlow bladeFlow : listR.getData()) {
								if (!bladeFlow.getComment().equals("") & !bladeFlow.getComment().equals("同意")) {
									HashMap<String, Object> userComment = new HashMap<>();
									userComment.put("user", bladeFlow.getAssigneeName());
									userComment.put("comment", bladeFlow.getComment());
									userCommentList.add(userComment);
								}
							}
						}
					}
				}
				boolean remark = request.uploadRemark(expert,userRealName,userCommentList);
				while(!remark) {
					errcnt.getAndIncrement();
					log.error("导出评论失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					remark = request.uploadRemark(expert,userRealName,userCommentList);
				}
				ExpertExtend expertExtend = new ExpertExtend();
				expertExtend.setId(expert.getExpertId());
				expertExtend.setMag(expert.getMag());
				expertExtend.setOtherHomepage(expert.getOtherHomepage());
				boolean extend = expertExtendService.saveOrUpdateToAminer(expertExtend);
				boolean result = work && edu && bio && bi && avatar && remark && extend ;
				if(!result){
					log.error("导出失败，失败专家id:"+expert.getExpertId());
				}
			}
		}//);
		R<Task> taskRes = taskClient.getById(taskId);
		if (taskRes.isSuccess()) {
			Task task = taskRes.getData();
			task.setStatus(TaskStatusEnum.EXPORTED.getNum());
			taskClient.saveTask(task);
		}
		return R.status(true);
	}

	/**
	 * 标注数据生效到aminer
	 */
	@GetMapping("/export-experts-aminer-by-fields")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "标注数据按字段生效到aminer", notes = "标注数据按字段生效到aminer")
	public R exportExpertsByFields(Long taskId, String fields){
		List<String> listField = Func.toStrList(fields);
		List<Expert> experts = expertMapper.queryExportExperts(taskId);


//		ArrayList<Expert> experts = new ArrayList<>();
//		Expert expert1 = new Expert();
//		expert1.setWork("中南大学湘雅三医院 博士生导师，一级主任医师");
//		expert1.setEdu("毕业于湖南医科大学");
//		expert1.setBio("dalao");
//		expert1.setBioZh("医学博士，博士生导师，一级主任医师，中南大学首届湘雅名医，全国医德标兵，中国最美女医师，湖南省十大同心人物，享受国务院政府特殊津贴。主要研究方向妇科肿瘤及妇科微无创技术。擅长达芬奇机器人手术。现担任国际微创与无创理事会理事，世界华人妇产科医师协会常务委员,中国医师协会微无创专业委员会副主任委员，中国医师协会微无创医学专业委员会第一届子宫肌瘤专业委员会（学组）主任委员，中国医师协会妇产科医师分会第三届委员会常务委员，湖南省医师协会第一届妇产科医师分会会长，湖南省医学会妇产科专业委员会副主任委员等国际国内学术职务20余项。担任《中南大学学报(医学版)》等7部杂志编委及审稿专家。近年主持及参与国家级课题5项,主持省自然科学基金重点项目、省自然科学基金及科技计划重点项目3项，省厅级一般科研课题20余项，国家专利20余项；在国内外期刊发表论文200余篇，其中SCI论文40余篇；主编/副主编专著10部、参编11部；获省级科技进步奖及省医学科学技术奖6项，省级教学成果奖1项。她是湖南省最早开展妇科腔镜技术的专家之一；她所领导的妇产科目前是湖南省唯一的宫内疾病微创诊治临床研究中心。她于2015年10月15日完成湖南省首例达芬奇机器人手术，在短短两个多月时间即创下2015年度全国妇科达芬奇机器人手术量单月第一的佳绩！也是国内首个妇科机器人手术单月完成达40例的妇产科专家。她2016年度及2017年度达芬奇机器人手术量居全国第一。她是在最短时间内完成手术种类最多的妇科术者之一，且实现了单人单日手术达7台的纪录。");
//		expert1.setPhone("234567");
//		expert1.setName("Min Xue");
//		expert1.setNameZh("李树春");
//		expert1.setExpertId("542a516fdabfae86fd95158b");
//		expert1.setTitles("0");
//		expert1.setTitlesDesc("董事长");
//		expert1.setPhone("0731-88638888%_%88618577%_%88618120");
//		expert1.setFax("0731—88921910");
//		expert1.setEmail("jasonlsc@126.com");
//		expert1.setAffiliation("The Third Xiangya Hospital of Central South University");
//		expert1.setAffiliationZh("中南大学湘雅三医院");
//		expert1.setHp("http://www.xy3yy.com/zjfc/fk2019/15670.html");
//		expert1.setHomepage("http://cpa.cqu.edu.cn/info/1071/3475.htm");
//		expert1.setGs("http://cpa.cqu.edu.cn/info/1071/3475.htm");s
//		expert1.setDblp("http://cpa.cqu.edu.cn/info/1071/3475.htm");
//		expert1.setGender("female");
//		expert1.setLanguage("chinese");
//		expert1.setId(1341597796091850753L);
//		expert1.setAddress("中央民族大学中国少数民族传统医学中心１号楼301室");
//		expert1.setAvatar("https://static.aminer.cn/upload/avatar/1306/1243/1303/542a516fdabfae86fd95158b_0.jpg");
//		expert1.setRemark("没有主页");
//		expert1.setUpdateUser(1341216104114147329L);
//		experts.add(expert1);

//		for(Expert expert:experts){
		ExportMagicRequest request = new ExportMagicRequest();
		AtomicInteger count = new AtomicInteger(0);
		AtomicInteger errcnt = new AtomicInteger(0);
//		experts.parallelStream().forEach(expert -> {

		for(Expert expertModified: experts) {
			count.getAndIncrement();
			Expert expertOrigin = expertService.importDetail("000000", expertModified.getExpertId(), expertModified.getTaskId());
			Expert expert = Objects.requireNonNull(BeanUtil.copy(expertOrigin, Expert.class));
			if (listField != null){
				listField.forEach(field-> BeanUtil.setProperty(expert, field, BeanUtil.getProperty(expertModified, field)));
			}
			log.info("upload expert: "+count);
			log.info("error count: "+errcnt);
			if(expert.getExpertId()!=null){
				boolean avatar = request.uploadAvatar(expert);
				while(!avatar) {
					errcnt.getAndIncrement();
					log.error("导出头像失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					avatar = request.uploadAvatar(expert);
				}
				boolean bi = request.uploadBasicInfo(expert);
				while(!bi) {
					errcnt.getAndIncrement();
					log.error("导出基本信息失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					bi = request.uploadBasicInfo(expert);
				}
				boolean work = request.uploadWork(expert);
				while(!work) {
					errcnt.getAndIncrement();
					log.error("导出工作经历失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					work = request.uploadWork(expert);
				}
				boolean edu = request.uploadEdu(expert);
				while(!edu) {
					errcnt.getAndIncrement();
					log.error("导出教育经历失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					edu = request.uploadEdu(expert);
				}
				boolean bio = request.uploadBio(expert);
				while(!bio) {
					errcnt.getAndIncrement();
					log.error("导出个人简介失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					bio = request.uploadBio(expert);
				}
				String userRealName;
				if(expert.getRemark()!=null){
					userRealName = expertService.queryNameById(expert.getUpdateUser()).getRealName();
				}else{
					userRealName = "任意值";
				}
				List<UserRemarkVO> userRemarkVOS = expertService.userRemark(expert.getId());
				List<UserRemarkVO> userInspectionRemarkVOS = expertService.userInspectionRemark(expert.getId());
				if(userInspectionRemarkVOS.size()!=0){
					userRemarkVOS.addAll(userInspectionRemarkVOS);
				}
				List<Map<String, Object>> userCommentList = new ArrayList<>();
				for(UserRemarkVO userRemarkVO:userRemarkVOS){
					if(userRemarkVO.getProcessInstanceId()!=null){
						R<List<BladeFlow>> listR = flowEngineClient.historyFlow(userRemarkVO.getProcessInstanceId(), "start", "end");
						if(listR.getData()!=null) {
							for (BladeFlow bladeFlow : listR.getData()) {
								if (!bladeFlow.getComment().equals("") & !bladeFlow.getComment().equals("同意")) {
									HashMap<String, Object> userComment = new HashMap<>();
									userComment.put("user", bladeFlow.getAssigneeName());
									userComment.put("comment", bladeFlow.getComment());
									userCommentList.add(userComment);
								}
							}
						}
					}
				}
				boolean remark = request.uploadRemark(expert,userRealName,userCommentList);
				while(!remark) {
					errcnt.getAndIncrement();
					log.error("导出评论失败，失败专家id:"+expert.getExpertId());
					ThreadUtil.sleep(2000);
					remark = request.uploadRemark(expert,userRealName,userCommentList);
				}
				ExpertExtend expertExtend = new ExpertExtend();
				expertExtend.setId(expert.getExpertId());
				expertExtend.setMag(expert.getMag());
				expertExtend.setOtherHomepage(expert.getOtherHomepage());
				boolean extend = expertExtendService.saveOrUpdateToAminer(expertExtend);
				boolean result = work && edu && bio && bi && avatar && remark && extend ;
				if(!result){
					log.error("导出失败，失败专家id:"+expert.getExpertId());
				}
			}
		}//);
		R<Task> taskRes = taskClient.getById(taskId);
		if (taskRes.isSuccess()) {
			Task task = taskRes.getData();
			task.setStatus(TaskStatusEnum.EXPORTED.getNum());
			taskClient.saveTask(task);
		}
		return R.status(true);
	}

	@GetMapping("/flow-remark")
	@ApiOperationSupport(order = 11)
	@ApiOperation(value = "工作流备注历史", notes = "工作流备注历史")
	public R<List<Map<String, Object>>> flowRemark(Long personId){
		List<UserRemarkVO> userRemarkVOS = expertService.userRemark(personId);
		List<Map<String, Object>> userCommentList = new ArrayList<>();
		for(UserRemarkVO userRemarkVO:userRemarkVOS){
			if(userRemarkVO.getProcessInstanceId()!=null){
				R<List<BladeFlow>> listR = flowEngineClient.historyFlow(userRemarkVO.getProcessInstanceId(),"start","end");
				if(listR.getData()!=null) {
					for (BladeFlow bladeFlow : listR.getData()) {
						if (!bladeFlow.getComment().equals("") & !bladeFlow.getComment().equals("同意")) {
							HashMap<String, Object> userComment = new HashMap<>();
							userComment.put("user", bladeFlow.getAssigneeName());
							userComment.put("comment", bladeFlow.getComment());
							userCommentList.add(userComment);
						}
					}
				}
			}
		}
		return R.data(userCommentList);
	}

	/**
	 * 导出专家
	 */
	@GetMapping("export-experts-excel")
	@ApiOperationSupport(order = 12)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "taskId", value = "查询条件", paramType = "query", dataType = "Long"),
	})
	@ApiOperation(value = "导出专家到excel", notes = "传入expert")
	public void exportUser(@ApiIgnore @RequestParam Map<String, Object> expert, HttpServletResponse response) {
		QueryWrapper<Expert> queryWrapper = Condition.getQueryWrapper(expert, Expert.class);
		queryWrapper.lambda().eq(Expert::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<ExpertExcel> list = expertService.exportExpert(queryWrapper);
		ExcelUtil.export(response, "专家数据" + DateUtil.time(), "专家数据表", list, ExpertExcel.class);
	}

	/**
	 * 导入专家
	 */
	@PostMapping("import-experts-excel")
	@ApiOperationSupport(order = 13)
	@ApiOperation(value = "从excel导入专家", notes = "传入excel")
	public R importExpert(MultipartFile file, Integer isCovered) {
		ExpertImporter expertImporter = new ExpertImporter(expertService, isCovered == 1);
		ExcelUtil.save(file, expertImporter, ExpertExcel.class);
		return R.success("操作成功");
	}

}
