package com.github.yarnchaosmonkey.killer;

import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExternalContainerKiller implements ContainerKiller {
	private static final Logger LOG = LoggerFactory.getLogger(ExternalContainerKiller.class);

	private String cmd;

	public ExternalContainerKiller(String cmd) {
		LOG.info("Starting external killer with cmd={} from cwd={}", cmd, System.getProperty("user.dir"));
		this.cmd = cmd;
	}

	@Override
	public void kill(ContainerReport container) throws Exception {
		Runtime rt = Runtime.getRuntime();
		String host = container.getAssignedNode().getHost();
		String command = cmd + " " + container.getContainerId() + " " + host + " " + getShortHost(host);
		LOG.info("Killing with command {}", command);
		Process pr = rt.exec(command);

		if(pr.waitFor() != 0) {
			throw new RuntimeException("Process exited with non-null return value "+pr.exitValue());
		}
	}

	protected static String getShortHost(String in) {
		return in.substring(0, in.indexOf("."));
	}

	/* public static void main(String[] args) {
		System.out.println("a = "+getShortHost("robert-streaming-w-29.c.astral-sorter-757.internal"));
	} */
}
