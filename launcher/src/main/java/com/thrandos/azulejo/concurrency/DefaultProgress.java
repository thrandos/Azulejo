/*
  ====================================================================         
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.

  You should have received a copy of the GNU General Public License
  along with Azulejo. If not, see https://www.gnu.org/licenses/.
  ====================================================================
*/

/* 
   =========================================================      
     _____   { ? }
    | .  .|
   DefaultProgress.java is a simple implementation of the 
   ProgressObservable interface, which is used to track 
   the progress of tasks in the launcher. It has two 
   properties: 

   STATUS (string)
   Describes current state of the task

   PROGRESS (double)
   Represents percentage of completion. Value of -1 
   indicates that the progress is indeterminate.
   ========================================================= 
*/

package com.thrandos.azulejo.concurrency;

import lombok.Data;

/**
 * A simple default implementation of {@link com.thrandos.azulejo.concurrency.ProgressObservable}
 * with settable properties.
 */
@Data
public class DefaultProgress implements ProgressObservable {

    private String status;
    private double progress = -1;

    public DefaultProgress() {
    }

    public DefaultProgress(double progress, String status) {
        this.progress = progress;
        this.status = status;
    }
}
