/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.loader.profiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.thrandos.azulejo.launcher.model.loader.InstallProcessor;
import com.thrandos.azulejo.launcher.model.loader.ProcessorEntry;
import com.thrandos.azulejo.launcher.model.loader.SidedData;
import com.thrandos.azulejo.launcher.model.minecraft.Library;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModernForgeInstallProfile {
    private int spec;
    private List<Library> libraries;
    private List<InstallProcessor> processors;
    private Map<String, SidedData<String>> data;
    private String minecraft;

    public List<ProcessorEntry> toProcessorEntries(final String loaderName) {
        return Lists.transform(getProcessors(), new Function<InstallProcessor, ProcessorEntry>() {
            @Override
            public ProcessorEntry apply(InstallProcessor input) {
                return new ProcessorEntry(loaderName, input);
            }
        });
    }
}
