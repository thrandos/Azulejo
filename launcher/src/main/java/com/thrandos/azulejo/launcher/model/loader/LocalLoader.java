package com.thrandos.azulejo.launcher.model.loader;

import com.thrandos.azulejo.launcher.model.modpack.DownloadableFile;
import lombok.Data;

import java.util.HashMap;

@Data
public class LocalLoader {
	private final LoaderManifest manifest;
	private final HashMap<String, DownloadableFile.LocalFile> localFiles;
}
