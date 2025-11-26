package com.thrandos.azulejo.launcher.auth.microsoft.model;

import lombok.Data;

@Data
public class XboxAuthorization {
	private final String token;
	private final String uhs;

	public String getCombinedToken() {
		return "%s;%s".formatted(uhs, token);
	}
}
