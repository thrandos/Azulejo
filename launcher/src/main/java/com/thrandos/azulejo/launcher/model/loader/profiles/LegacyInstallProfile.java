package com.thrandos.azulejo.launcher.model.loader.profiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thrandos.azulejo.launcher.model.loader.VersionInfo;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyInstallProfile {
	@JsonProperty("install")
	private InstallData installData;
	private VersionInfo versionInfo;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class InstallData {
		private String path;
		private String filePath;
	}
}
