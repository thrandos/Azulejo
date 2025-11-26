package com.thrandos.azulejo.launcher.auth.skin;

import com.thrandos.azulejo.launcher.util.HttpRequest;
import lombok.extern.java.Log;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.logging.Level;

import static com.thrandos.azulejo.launcher.util.HttpRequest.url;

@Log
public class VisageSkinService {
	@Nullable
	public static byte[] fetchSkinHead(String uuid) throws InterruptedException {
		String skinUrl = "https://visage.surgeplay.com/face/32/%s.png".formatted(uuid);

		try {
			return HttpRequest.get(url(skinUrl))
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asBytes();
		} catch (IOException e) {
			log.log(Level.WARNING, "Failed to download or process skin from Visage.", e);
			return null;
		}
	}
}
