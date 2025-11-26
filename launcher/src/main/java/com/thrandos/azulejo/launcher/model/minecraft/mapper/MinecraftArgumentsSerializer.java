package com.thrandos.azulejo.launcher.model.minecraft.mapper;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.thrandos.azulejo.launcher.model.minecraft.GameArgument;

import java.io.IOException;

public class MinecraftArgumentsSerializer extends StdSerializer<GameArgument> {
	protected MinecraftArgumentsSerializer() {
		super(GameArgument.class);
	}

	@Override
	public void serialize(GameArgument value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
		if (value.getValues().size() == 1 && (value.getRules() == null || value.getRules().size() == 0)) {
			jgen.writeString(value.getValues().getFirst());
		} else {
			provider.defaultSerializeValue(value, jgen);
		}
	}
}
