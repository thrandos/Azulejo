/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.minecraft.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.thrandos.azulejo.launcher.util.Platform;

import java.io.IOException;

public class PlatformSerializer extends JsonSerializer<Platform> {

    @Override
    public void serialize(Platform platform, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        switch (platform) {
            case WINDOWS:
                jsonGenerator.writeString("windows");
                break;
            case MAC_OS_X:
                jsonGenerator.writeString("osx");
                break;
            case LINUX:
                jsonGenerator.writeString("linux");
                break;
            case SOLARIS:
                jsonGenerator.writeString("solaris");
                break;
            case UNKNOWN:
                jsonGenerator.writeNull();
                break;
        }
    }

}
