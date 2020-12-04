package org.springblade.subtask;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.launch.BladeApplication;
import org.springframework.cloud.client.SpringCloudApplication;


@SpringCloudApplication
public class SubTaskApplication {
	public static void main(String[] args) {
		BladeApplication.run(LauncherConstant.APPLICATION_SUBTASK_NAME, SubTaskApplication.class, args);
	}
}
