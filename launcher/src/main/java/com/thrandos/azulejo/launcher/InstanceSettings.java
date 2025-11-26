package com.thrandos.azulejo.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thrandos.azulejo.launcher.launch.MemorySettings;
import com.thrandos.azulejo.launcher.launch.runtime.JavaRuntime;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceSettings {
	private JavaRuntime runtime;
	private MemorySettings memorySettings;
	private String customJvmArgs;
}
