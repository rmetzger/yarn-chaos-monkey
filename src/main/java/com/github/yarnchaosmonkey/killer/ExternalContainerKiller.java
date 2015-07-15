package com.github.yarnchaosmonkey.killer;

import org.apache.hadoop.yarn.api.records.ContainerReport;

import java.io.IOException;

public class ExternalContainerKiller implements ContainerKiller {

	private String cmd;

	public ExternalContainerKiller(String cmd) {
		this.cmd = cmd;
	}

	@Override
	public void kill(ContainerReport container) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd+" "+container.getContainerId()+" "+container.getAssignedNode().getHost());

		if(pr.exitValue() != 0) {
			throw new RuntimeException("Process exited with non-null return value "+pr.exitValue());
		}
	}
}
