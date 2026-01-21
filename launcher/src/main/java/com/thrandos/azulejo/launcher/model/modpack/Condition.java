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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="if")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RequireAny.class, name = "requireAny"),
        @JsonSubTypes.Type(value = RequireAll.class, name = "requireAll")
})
public interface Condition {

    boolean matches();

}
