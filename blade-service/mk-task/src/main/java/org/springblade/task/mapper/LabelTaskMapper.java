package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.composition.entity.Composition;
import org.springblade.task.vo.CompositionClaimCountVO;
import org.springblade.task.vo.CompositionClaimListVO;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.vo.RoleClaimCountVO;

import java.util.List;

public interface LabelTaskMapper extends BaseMapper<LabelTask> {

	/**
	 * 已完成子任务数
	 *
	 * @param taskId    任务Id
	 * @return
	 */
	int completeCount(@Param("taskId")Long taskId);

	/**
	 * 查询完成子任务列表
	 *
	 * @param taskId    任务Id
	 * @return
	 */
	List<LabelTask> queryCompleteTask(@Param("taskId")Long taskId);

	/**
	 * 根据专家id查对应子任务流程实例id
	 * @param expertId    专家真正的id
	 * @return
	 */
	List<ExpertLabelTaskVO> personIdToProcessInstance(String expertId);

	/**
	 * 查询用户标注任务完成数量
	 * @param userName  用户名
	 * @return
	 */
	long annotationDoneCount(@Param("userName")String userName);

	/**
	 * 查询用户标注任务待办数量
	 * @param userName  用户名
	 * @return
	 */
	long annotationTodoCount(@Param("userName")String userName);

	/**
	 * 查询用户标注任务待签数量
	 * @param roleAlias  角色列表
	 * @return
	 */
	int annotationClaimCount(@Param("roleAlias")List<String> roleAlias);

	/**
	 * 查询用户标注任务待签数量
	 * @param roleAlias  角色列表
	 * @param userId  用户Id
	 * @return
	 */
	int annotationClaimCount2(@Param("roleAlias")List<String> roleAlias, @Param("userId")Long userId);

	/**
	 * 返回当前用户所有角色及分别可接的任务数
	 * @param roleAlias  角色列表
	 * @param userId  用户Id
	 * @return
	 */
	List<RoleClaimCountVO> roleClaimCount(@Param("roleAlias")List<String> roleAlias, @Param("userId")Long userId);

	/**
	 * 返回当前用户所有组合及分别可接的任务数
	 * @param roleAlias  角色列表
	 * @param userId  用户Id
	 * @return
	 */
	List<CompositionClaimCountVO> compositionClaimCount(@Param("roleAlias")List<String> roleAlias, @Param("userId")Long userId);

	/**
	 * 返回当前用户所有组合及分别可接的任务列表
	 * @param roleAlias  角色列表
	 * @param userId  用户Id
	 * @return
	 */
	List<CompositionClaimListVO> compositionClaimList(@Param("roleAlias")List<String> roleAlias, @Param("userId")Long userId);

	/**
	 * 返回模版中所有的组合
	 * @param templateId 模版Id
	 * @return
	 */
	List<Composition> allCompositions(@Param("templateId")Long templateId);
}
