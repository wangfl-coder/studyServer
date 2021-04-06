package org.springblade.mq.rabbit.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import jodd.util.ThreadUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.ExpertMerge;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.adata.feign.IExpertMergeClient;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Holder;
import org.springblade.mq.rabbit.constant.RabbitConstant;
import org.springblade.mq.rabbit.entity.PurePerson;
import org.springblade.mq.rabbit.entity.SupPerson;
import org.springblade.mq.rabbit.magic.MagicRequest;
import org.springblade.mq.rabbit.message.MessageStruct;
import org.springblade.mq.rabbit.service.IPurePersonService;
import org.springblade.mq.rabbit.service.ISupPersonService;
import org.springblade.task.entity.Task;
import org.springblade.task.enums.MergeExpertTaskStatusEnum;
import org.springblade.task.feign.IMergeExpertTaskClient;
import org.springblade.task.feign.ITaskClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * 直接队列1 处理器
 *
 * @author yangkai.shen
 */
@Slf4j
@RabbitListener(queues = RabbitConstant.DIRECT_MODE_QUEUE_PREPROCESS_PURE_SUP_PERSON)
@Component
@AllArgsConstructor
public class PreprocessPureSupHandler {

	private final IPurePersonService purePersonService;
	private final ISupPersonService supPersonService;
	private final IMergeExpertTaskClient mergeExpertTaskClient;
	private final ITaskClient taskClient;
	private final IExpertClient expertClient;
	private final IExpertMergeClient expertMergeClient;

	/**
	 * 如果 spring.rabbitmq.listener.direct.acknowledge-mode: auto，则可以用这个方式，会自动ack
	 */
	// @RabbitHandler
	public void directHandlerAutoAck(MessageStruct message) {
		log.info("直接队列处理器，接收消息：{}", JsonUtil.toJson(message));
	}

	@RabbitHandler
	public void directHandlerManualAck(MessageStruct messageStruct, Message message, Channel channel) {
		//  如果手动ACK,消息会被监听消费,但是消息在队列中依旧存在,如果 未配置 acknowledge-mode 默认是会在消费完毕后自动ACK掉
		final long deliveryTag = message.getMessageProperties().getDeliveryTag();
		try {
			log.info("pure.sup.person队列，手动ACK，接收消息：{}", JsonUtil.toJson(messageStruct));
//			ThreadUtil.sleep(10000);
			String msg = messageStruct.getMessage();
			List<String> idList = Arrays.asList(msg.split(","));
			String taskId = idList.get(0);
			String mergeTaskId = idList.get(1);
			String expertId = idList.get(2);
			String personId = idList.get(3);

			R<Task> taskRes = taskClient.getById(Long.valueOf(taskId));
			if (!taskRes.isSuccess()) {
				throw new Exception();
			}
			Task task = taskRes.getData();

			String pureRes = pureOnePerson(expertId);
			//解析json,拿到每个学者的id
			JSONObject resObj = JSON.parseObject(pureRes);
			JSONArray dataArray = resObj.getJSONArray("data");
			JSONObject dataRes = dataArray.getJSONObject(0);
			boolean importSuccess = dataRes.getBoolean("succeed");
			if (!importSuccess) {
				throw new Exception();
			}
			JSONArray itemArray = dataRes.getJSONArray("items");
			if (itemArray != null) {
				JSONObject pureDataRes = itemArray.getJSONObject(0);
				JSONObject error_names = pureDataRes.getJSONObject("error_names");
				JSONArray need_repair_papers = pureDataRes.getJSONArray("need_repair_papers");
				JSONArray papers_to_person = pureDataRes.getJSONArray("papers_to_person");
				JSONArray remove_direct = pureDataRes.getJSONArray("remove_direct");
				JSONArray remove_from_person = pureDataRes.getJSONArray("remove_from_person");
				PurePerson purePerson = new PurePerson();
				if (error_names != null)
					purePerson.setErrorNames(error_names.toString());
				if (need_repair_papers != null)
					purePerson.setNeedRepairPapers(need_repair_papers.toString());
				if (papers_to_person != null)
					purePerson.setPapersToPerson(papers_to_person.toString());
				if (remove_direct != null)
					purePerson.setRemoveDirect(remove_direct.toString());
				if (remove_from_person != null)
					purePerson.setRemoveFromPerson(remove_from_person.toString());
				purePerson.setTenantId(task.getTenantId());
				purePerson.setTaskId(Long.valueOf(taskId));
				purePerson.setMergeTaskId(Long.valueOf(mergeTaskId));
				purePerson.setExpertId(expertId);
				purePerson.setPersonId(Long.valueOf(personId));
				purePersonService.save(purePerson);
			}

//			String supRes = supOnePerson(expertId);
//			//解析json,拿到每个学者的id
//			JSONObject resObj2 = JSON.parseObject(supRes);
//			JSONArray dataArray2 = resObj2.getJSONArray("data");
//			JSONObject dataRes2 = dataArray2.getJSONObject(0);
//			boolean importSuccess2 = dataRes2.getBoolean("succeed");
//			if (!importSuccess2) {
//				throw new Exception();
//			}
//			JSONObject keyValues = dataRes.getJSONObject("keyValues");
//			if (keyValues != null) {
//				JSONObject papers = keyValues.getJSONObject("papers");
//				JSONObject persons = keyValues.getJSONObject("persons");
//				SupPerson supPerson = new SupPerson();
//				if (papers != null)
//					supPerson.setPapers(papers.toString());
//				if (persons != null)
//					supPerson.setPersons(persons.toString());
//				supPerson.setTenantId(task.getTenantId());
//				supPerson.setTaskId(Long.valueOf(taskId));
//				supPerson.setMergeTaskId(Long.valueOf(mergeTaskId));
//				supPerson.setExpertId(expertId);
//				supPerson.setPersonId(Long.valueOf(personId));
//				supPersonService.save(supPerson);
//
//				for (Map.Entry<String,Object> entry : persons.entrySet()) {
//					String expert_id = entry.getKey();            //	expert_id
//					Integer score = (Integer)entry.getValue();    //	score
//
//				}
//
//			}
			final Random random = Holder.RANDOM;
			SupPerson supPerson = new SupPerson();
			supPerson.setTenantId(task.getTenantId());
			supPerson.setTaskId(Long.valueOf(taskId));
			supPerson.setMergeTaskId(Long.valueOf(mergeTaskId));
			supPerson.setExpertId(expertId);
			supPerson.setPersonId(Long.valueOf(personId));
			if (random.nextInt(100) < 50) {
				supPerson.setPapers("{\"53e99842b7602d970206d905\":1,\"53e99937b7602d97021725cf\":1,\"53e99984b7602d97021c440f\":1,\"53e99c6eb7602d970251bfac\":0.963994562625885,\"53e99d28b7602d97025df761\":1,\"53e9a06db7602d9702956153\":0.9864528179168701,\"53e9a644b7602d9702f708e3\":0.9962676763534546,\"53e9ab55b7602d97034edf1d\":0.982132077217102,\"53e9abdab7602d9703592c77\":1,\"53e9b365b7602d9703e48d0e\":1,\"53e9bbc8b7602d9704816807\":1,\"5488dace45ce147a86dc913b\":1,\"5488dacf45ce147a86dc919f\":1,\"5488dbb845ce147a86dd4194\":1,\"5488de8145ce147a86def796\":1,\"5488df0345ce147a86df24a8\":1,\"5488dfc545ce147a86df6d61\":1,\"5488e28e45ce147a86e19dfb\":1,\"5488e65c45ce147a86e6046c\":1,\"5488e66a45ce147a86e61958\":1,\"5488e6a645ce147a86e6766c\":1,\"5488e6a645ce147a86e676a0\":1,\"5488e8b545ce471f908fb00c\":1,\"5488e8b645ce471f908fb072\":1,\"5488e9dd45ce471f9090b274\":1,\"5488e9e145ce471f9090c273\":1,\"5488e9e145ce471f9090c379\":1,\"5488e9e745ce471f9090db4f\":1,\"5488f16545ce471f9095d7e2\":1,\"5488fa8045ce471f909ae626\":0.9885047674179077,\"5489121645ce471f90aece0d\":1,\"5489128a45ce471f90af7e68\":1,\"5489130945ce471f90b0667c\":1,\"5489132f45ce471f90b0bad5\":1,\"5489136345ce471f90b1015b\":1,\"5489138445ce471f90b126b9\":1,\"5489144f45ce471f90b1cee9\":1,\"56d84208dabfae2eee904160\":0.9649239778518677,\"56d8e6dedabfae2eee36e54c\":1,\"56d908abdabfae2eee07c41d\":1,\"56d908addabfae2eee07cfdd\":1,\"5c7561caf56def9798d8a01b\":1,\"5c75680df56def97981caa1f\":1,\"5c756936f56def9798272452\":1,\"5c756a69f56def9798338ab8\":1,\"5c756dd8f56def979856d1e7\":1,\"5c7570dbf56def979872e3ab\":1,\"5c75731ef56def979886d73b\":1,\"5c7573c5f56def97988d3cf2\":0.992215096950531,\"5c75741af56def979890ce69\":1,\"5c7d3ff64895d9cbc6a0bcea\":0.9225945472717285,\"5c89c0554895d9cbc6d5c8ca\":1,\"5ce2cff9ced107d4c632af03\":0.9971507787704468,\"5ce2d02aced107d4c634cdaa\":1,\"5ce2d073ced107d4c637dcca\":1,\"5ce2d078ced107d4c638112f\":1,\"5ce2d114ced107d4c63ebc23\":1,\"5ce2d13aced107d4c6405c99\":1,\"5ce2d1d3ced107d4c646e9cd\":1,\"5ce2d1ebced107d4c647f32e\":1,\"5ce2d219ced107d4c649e1cd\":0.9855601787567139,\"5d0b00218607575390facf5f\":1,\"5d0b01018607575390ff4022\":1,\"5d9ed14947c8f76646f49a06\":1,\"5e09a9e4df1a9c0c416b05f0\":1,\"5e5e196193d709897ce691c1\":1,\"5e5e198893d709897ce732ea\":1,\"5e7233f993d709897cfb837e\":1,\"5e72343793d709897cfbf444\":1,\"5e72355293d709897cfe8955\":1,\"5fd57c9da4e4c3c831a329aa\":0.8719707131385803,\"603463429e795e1d55f80823\":0.9145359992980957,\"605c08959e795e05e0fd6cca\":0.2300044596195221}");
				supPerson.setPersons("{\"53f5693cdabfae6707f8049d\":1,\"5603004c45cedb339609a8f7\":1,\"56069c3745cedb33968e9784\":1,\"5607fb0045cedb3396bc0ad0\":1,\"560a30d545cedb33970621af\":1,\"560b9ea045cedb339737bc87\":1,\"560cfdc345cedb3397594c2d\":1,\"560d821845cedb33975ecd9b\":1,\"560d9f7445cedb339760094d\":1,\"56175e4b45cedb3397bea8fe\":1}");
			} else {
				supPerson.setPapers("{\"53e9a108b7602d97029f4f00\":1,\"53e9a357b7602d9702c61019\":0.9898198843002319,\"5fd57c8da4e4c3c831a31777\":0.9921051263809204}");
				supPerson.setPersons("{\"562dec6045ce1e5967b4eedf\":1}");
			}

			supPersonService.save(supPerson);

			JSONObject persons = JSON.parseObject(supPerson.getPersons());
			for (Map.Entry<String,Object> entry : persons.entrySet()) {
				String expert_id = entry.getKey();            //	expert_id
				Integer score = (Integer)entry.getValue();    //	score
				R<Expert> expertRes = expertClient.fetchExpertDetail(task.getTenantId(), expert_id, task.getId());
				if (expertRes.isSuccess()) {
					Expert expert = expertRes.getData();
					ExpertMerge expertMerge = Objects.requireNonNull(BeanUtil.copy(expert, ExpertMerge.class));
					expertMerge.setId(null);
					expertMerge.setCreateUser(null);
					expertMerge.setCreateDept(null);
					expertMerge.setUpdateUser(null);
					expertMerge.setStatus(null);
					expertMerge.setIsDeleted(null);
					expertMerge.setLabelPersonId(Long.valueOf(personId));
					expertMergeClient.saveExpert(expertMerge);
				}
			}

			mergeExpertTaskClient.changeStatus(Long.valueOf(mergeTaskId), MergeExpertTaskStatusEnum.PURE_SUPED.getNum());
			// 通知 MQ 消息已被成功消费,可以ACK了
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			try {
				// 处理失败,重新压入MQ
				//channel.basicRecover();
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private String pureOnePerson(String id) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		parameters.put("id", id);
		parameters.put("dl_check", false);

		body.put("action", "na.PureOnePerson");
		body.put("parameters", parameters);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return res;
	}

	private String supOnePerson(String id) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		parameters.put("id", id);

		body.put("action", "na.SupOnePerson");
		body.put("parameters", parameters);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return res;
	}
}
