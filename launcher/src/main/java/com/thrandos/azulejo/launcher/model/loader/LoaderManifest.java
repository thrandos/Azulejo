package com.thrandos.azulejo.launcher.model.loader;

import com.thrandos.azulejo.launcher.model.minecraft.Library;
import com.thrandos.azulejo.launcher.model.modpack.DownloadableFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoaderManifest {
	private List<Library> libraries;
	private Map<String, SidedData<String>> sidedData;
	private List<DownloadableFile> downloadableFiles;

	public Library findLibrary(String name) {
		for (Library library : getLibraries()) {
			if (library.matches(name)) {
				return library;
			}
		}

		return null;
	}
}
