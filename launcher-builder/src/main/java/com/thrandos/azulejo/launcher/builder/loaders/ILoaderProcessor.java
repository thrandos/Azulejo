package com.thrandos.azulejo.launcher.builder.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thrandos.azulejo.launcher.model.modpack.Manifest;

import java.io.File;
import java.io.IOException;

public interface ILoaderProcessor {
	LoaderResult process(File loaderJar, Manifest manifest, ObjectMapper mapper, File baseDir) throws IOException;
}
