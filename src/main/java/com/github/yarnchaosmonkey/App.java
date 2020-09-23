package com.github.yarnchaosmonkey;

import com.github.yarnchaosmonkey.killer.ContainerKiller;
import com.github.yarnchaosmonkey.killer.ExternalContainerKiller;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationAttemptState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class App  {
	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws Exception {

		ParameterTool pt = ParameterTool.fromArgs(args);

		ContainerKiller killer = new ExternalContainerKiller(pt.get("scriptPath", "scripts/remoteKill.sh"));

		YarnClient yc = YarnClient.createYarnClient();
		yc.init(new Configuration());
		yc.start();

		ApplicationId appid = ConverterUtils.toApplicationId(pt.getRequired("appId"));
		if(appid == null) {
			throw new IllegalArgumentException("Can not parse appId: " + pt.getRequired("appId"));
		}
		List<ApplicationAttemptReport> attempts = yc.getApplicationAttempts(appid);

		LOG.info("The application " + appid + " has the following attempts: " + attempts);

		ApplicationAttemptReport runningAttempt = null;
		for (ApplicationAttemptReport attempt: attempts) {
			if(attempt.getYarnApplicationAttemptState() == YarnApplicationAttemptState.RUNNING) {
				runningAttempt = attempt;
				LOG.info("Using attempt " + attempt);
			} else {
				LOG.info("Can not use attempt " + attempt);
			}
		}
		if(runningAttempt == null) {
			throw new RuntimeException("Unable to find running attempt");
		}

		long sleepTime = pt.getLong("sleepBetweenFailuresSec")*1000;
		// enter the loop
		Random rnd = new Random(1337);

		while(true) {
			List<ContainerReport> containers = yc.getContainers(runningAttempt.getApplicationAttemptId());
			LOG.info("Identified " + containers.size() + " running containers of " + appid);

			// find a container to kill
			ContainerReport containerToKill = null;
			while(containerToKill == null) {
				int idx = rnd.nextInt(containers.size() - 1);
				containerToKill = containers.get(idx);
				LOG.info("Selected " + containerToKill + " as container to kill");
				if(!pt.has("killAM") && containerToKill.getContainerId().getContainerId() == runningAttempt.getAMContainerId().getContainerId()) {
					LOG.info("Container to kill is AM. Looking for another container");
					containerToKill = null;
					if(containers.size() == 1) {
						LOG.info("Stopping tool. There is only one container (the AM) and I'm not allowed to kill the AM");
						return;
					}
				}
			}
			// kill container
			killer.kill(containerToKill);

			LOG.info("Sleeping for "+(sleepTime/60)+" seconds");
			// wait for the next round
			Thread.sleep(sleepTime);
		}
	}
}
