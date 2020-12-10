package org.springblade.task;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.launch.BladeApplication;
import org.springframework.cloud.client.SpringCloudApplication;


@SpringCloudApplication
public class MkTaskApplication {
	public static void main(String[] args) {
		BladeApplication.run(LauncherConstant.MKAPP_TASK_NAME, MkTaskApplication.class, args);
	}
}
