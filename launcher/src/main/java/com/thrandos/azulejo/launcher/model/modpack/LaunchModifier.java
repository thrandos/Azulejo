/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.modpack;

import com.google.common.collect.Lists;
import com.thrandos.azulejo.launcher.launch.JavaProcessBuilder;
import lombok.Data;

import java.util.List;

@Data
public class LaunchModifier {

    private List<String> flags = Lists.newArrayList();

    public void setFlags(List<String> flags) {
        this.flags = flags != null ? flags : Lists.<String>newArrayList();
    }

    public void modify(JavaProcessBuilder builder) {
        if (flags != null) {
            for (String flag : flags) {
                builder.getFlags().add(flag);
            }
        }
    }
}
