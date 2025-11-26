package com.thrandos.azulejo.launcher.auth.skin;

import com.thrandos.azulejo.launcher.auth.microsoft.model.McProfileResponse;
import com.thrandos.azulejo.launcher.util.HttpRequest;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class MinecraftSkinService {
	static byte[] downloadSkin(String textureUrl) throws IOException, InterruptedException {
		return HttpRequest.get(HttpRequest.url(textureUrl))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asBytes();
	}

	public static byte[] fetchSkinHead(McProfileResponse profile) throws InterruptedException {
		try {
			byte[] skin = downloadSkin(profile.getActiveSkin().getUrl());

			return SkinProcessor.renderHead(skin);
		} catch (IOException e) {
			log.log(Level.WARNING, "Failed to download or process skin.", e);
			return null;
		}
	}
}
