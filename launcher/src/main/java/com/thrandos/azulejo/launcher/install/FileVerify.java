package com.thrandos.azulejo.launcher.install;

import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.LauncherException;
import com.thrandos.azulejo.launcher.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;

@RequiredArgsConstructor
@Log
public class FileVerify implements InstallTask {
	private final File target;
	private final String name;
	private final String hash;

	@Override
	public void execute(Launcher launcher) throws Exception {
		log.info("Verifying file " + name);

		String actualHash = FileUtils.getShaHash(target);
		if (!actualHash.equals(hash)) {
			String message = """
                    File %s (%s) is corrupt (invalid hash)
                    Expected '%s'
                    Got '%s'""".formatted(
                    name, target.getAbsolutePath(), hash, actualHash);

			throw new LauncherException(message, message);
		}
	}

	@Override
	public double getProgress() {
		return -1;
	}

	@Override
	public String getStatus() {
		return "Verifying " + name;
	}
}
