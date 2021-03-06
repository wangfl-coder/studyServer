package org.springblade.task.service;

import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.support.Kv;
import org.springblade.task.vo.CompositionClaimCountVO;
import org.springblade.task.vo.CompositionClaimListVO;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.Task;
import org.springblade.task.vo.RoleClaimCountVO;

import java.util.List;
import java.util.Map;

public interface LabelTaskService extends BaseService<LabelTask> {

	boolean startProcess(String ProcessDefinitionId,
						 Task task,
						 List<Expert> experts);

	LabelTask startFixProcess(String ProcessDefinitionId,
						 Task task,
						 Expert expert);

	Map<String, String> startRealSetProcess(String realSetProcessDefinitions,
										Task task);

	int completeCount(Long taskId);
//
//	List<TaskVO> getCompleteTaskCount(List<Task> tasks);
    List<ExpertLabelTaskVO> personIdToProcessInstance(String expertId);

	List<LabelTask> queryCompleteTask(Long taskId);

	List<LabelTask> queryUniqueCompleteTask(Long taskId);

	List<LabelTask> getByTaskId(Long taskId);

	long annotationDoneCount(String param2);

	long annotationTodoCount(String param2);

	int annotationClaimCount(List<String> param2);

	/**
	 * 返回当前用户所有角色及分别可接的任务数
	 *
	 * @param roleAlias
	 * @return
	 */
	List<RoleClaimCountVO> roleClaimCount(List<String> roleAlias);

	/**
	 * 返回当前用户所有组合及分别可接的任务数
	 *
	 * @param roleAlias
	 * @return
	 */
	List<CompositionClaimCountVO> compositionClaimCount(List<String> roleAlias);

	/**
	 * 返回当前用户所有组合及分别可接的任务列表
	 *
	 * @param roleAliases
	 * @return
	 */
	List<CompositionClaimListVO> compositionClaimList(List<String> roleAliases);
}
