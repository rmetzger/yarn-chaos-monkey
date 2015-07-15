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
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class App  {
	public static void main(String[] args) throws IOException, YarnException, InterruptedException {

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

		System.out.println("The application "+appid+" has the following attempts: "+attempts);

		ApplicationAttemptReport runningAttempt = null;
		for (ApplicationAttemptReport attempt: attempts) {
			if(attempt.getYarnApplicationAttemptState() == YarnApplicationAttemptState.RUNNING) {
				runningAttempt = attempt;
				System.out.println("Using attempt "+attempt);
			} else {
				System.out.println("Can not use attempt "+attempt);
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
			System.out.println("Identified " + containers.size() + " running containers of " + appid);

			// find a container to kill
			ContainerReport containerToKill = null;
			while(containerToKill == null) {
				int idx = rnd.nextInt(containers.size() - 1);
				containerToKill = containers.get(idx);
				System.out.println("Selected " + containerToKill + " as container to kill");
				if(!pt.has("killAM") && containerToKill.getContainerId().getContainerId() == runningAttempt.getAMContainerId().getContainerId()) {
					System.out.println("Container to kill is AM. Looking for another container");
					containerToKill = null;
					if(containers.size() == 1) {
						System.out.println("Stopping tool. There is only one container (the AM) and I'm not allowed to kill the AM");
						return;
					}
				}
			}
			// kill container
			killer.kill(containerToKill);

			// wait for the next round
			Thread.sleep(sleepTime);
		}
	}
}
