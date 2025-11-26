package com.thrandos.azulejo.launcher.auth.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class XboxAuthResponse {
	private String token;
	private DisplayClaims displayClaims;

	@JsonIgnore
	public String getUhs() {
		return getDisplayClaims().getXui().getFirst().getUhs();
	}

	@Data
	public static class DisplayClaims {
		private List<UhsContainer> xui;
	}

	@Data
	public static class UhsContainer {
		private String uhs;
	}
}
