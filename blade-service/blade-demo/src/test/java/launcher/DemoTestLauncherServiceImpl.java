package launcher;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.auto.service.AutoService;
import org.springblade.core.launch.service.LauncherService;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

@AutoService(LauncherService.class)
public class DemoTestLauncherServiceImpl implements LauncherService {

	@Override
	public void launcher(SpringApplicationBuilder builder, String appName, String profile, boolean isLocalDev){
		Properties props = System.getProperties();
		props.setProperty("spring.cloud.nacos.discover.server-addr", LauncherConstant.NACOS_TEST_ADDR);
		props.setProperty("spring.cloud.nacos.config.server-addr", LauncherConstant.NACOS_TEST_ADDR);
		props.setProperty("spring.cloud.sentinel.transport.dashboard", LauncherConstant.NACOS_DEV_ADDR);
	}

	@Override
	public int getOrder(){
		return 10;
	}
}
