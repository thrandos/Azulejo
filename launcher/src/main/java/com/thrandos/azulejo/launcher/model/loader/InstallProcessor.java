package com.thrandos.azulejo.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.thrandos.azulejo.launcher.model.minecraft.Side;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstallProcessor {
	private String jar;
	private List<String> classpath;
	private List<String> args;
	private Map<String, String> outputs;
	private List<String> sides;

	public List<String> resolveArgs(LoaderSubResolver resolver) {
		return Lists.transform(getArgs(), resolver);
	}

	public Map<String, String> resolveOutputs(final LoaderSubResolver resolver) {
		if (getOutputs() == null) return Collections.emptyMap();

		HashMap<String, String> result = new HashMap<String, String>();

		for (Map.Entry<String, String> entry : getOutputs().entrySet()) {
			result.put(resolver.apply(entry.getKey()), resolver.apply(entry.getValue()));
		}

		return result;
	}

	public boolean shouldRunOn(Side side) {
		if (sides == null) {
			return true;
		}

		return switch (side) {
			case CLIENT -> sides.contains("client");
			case SERVER -> sides.contains("server");
		};
	}
}
