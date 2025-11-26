/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.model.creator;

import lombok.Data;

@Data
public class Problem {

    private final String title;
    private final String explanation;

    public Problem(String title, String explanation) {
        this.title = title;
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return title;
    }

}
