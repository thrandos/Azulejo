package com.thrandos.azulejo.launcher.builder.loaders;

import com.google.common.collect.Lists;
import com.thrandos.azulejo.launcher.model.minecraft.Library;
import lombok.Data;

import java.net.URL;
import java.util.List;

@Data
public class LoaderResult {
	private final List<Library> loaderLibraries = Lists.newArrayList();
	private final List<Library> processorLibraries = Lists.newArrayList();
	private final List<URL> jarMavens = Lists.newArrayList();
}
