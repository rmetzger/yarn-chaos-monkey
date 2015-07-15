package com.github.yarnchaosmonkey.killer;

import org.apache.hadoop.yarn.api.records.ContainerReport;

import java.io.IOException;

public interface ContainerKiller {

	void kill(ContainerReport container) throws IOException;
}
