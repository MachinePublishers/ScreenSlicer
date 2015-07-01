/* 
 * ScreenSlicer (TM)
 * Copyright (C) 2013-2015 Machine Publishers, LLC
 * ops@machinepublishers.com | screenslicer.com | machinepublishers.com
 * Cincinnati, Ohio, USA
 *
 * You can redistribute this program and/or modify it under the terms of the GNU Affero General Public
 * License version 3 as published by the Free Software Foundation.
 *
 * ScreenSlicer is made available under the terms of the GNU Affero General Public License version 3
 * with the following clarification and special exception:
 *
 *   Linking ScreenSlicer statically or dynamically with other modules is making a combined work
 *   based on ScreenSlicer. Thus, the terms and conditions of the GNU Affero General Public License
 *   version 3 cover the whole combination.
 *
 *   As a special exception, Machine Publishers, LLC gives you permission to link unmodified versions
 *   of ScreenSlicer with independent modules to produce an executable, regardless of the license
 *   terms of these independent modules, and to copy, distribute, and make available the resulting
 *   executable under terms of your choice, provided that you also meet, for each linked independent
 *   module, the terms and conditions of the license of that module. An independent module is a module
 *   which is not derived from or based on ScreenSlicer. If you modify ScreenSlicer, you may not
 *   extend this exception to your modified version of ScreenSlicer.
 *
 * "ScreenSlicer", "jBrowserDriver", "Machine Publishers", and "automatic, zero-config web scraping"
 * are trademarks of Machine Publishers, LLC.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License version 3 for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License version 3 along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For general details about how to investigate and report license violations, please see:
 * <https://www.gnu.org/licenses/gpl-violation.html> and email the author: ops@machinepublishers.com
 */
package com.screenslicer.core.scrape.neural;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.screenslicer.common.Log;
import com.screenslicer.webapp.WebApp;

public class NeuralNetManager {
  private static NeuralNetVoters[] net = new NeuralNetVoters[WebApp.THREADS];
  static {
    for (int i = 0; i < WebApp.THREADS; i++) {
      net[i] = new NeuralNetVoters();
    }
  }

  private NeuralNetManager() {

  }

  public static NeuralNet instance(int thread) {
    return net[thread];
  }

  public static String asString() {
    return net.toString();
  }

  public static NeuralNet randomInstance(int numInputs,
      int numNets, int numLayers, int numNodesPerLayer) {
    for (int i = 0; i < numNets; i++) {
      if (i == 0) {
        reset(new NeuralNetVote(NeuralNetProperties.randomInstance(
            numInputs, numLayers, numNodesPerLayer)), 0);
      } else {
        add(new NeuralNetVote(NeuralNetProperties.randomInstance(
            numInputs, numLayers, numNodesPerLayer)), 0);
      }
    }
    return net[0];
  }

  public static void add(String config, int thread) {
    if (config != null) {
      net[thread].add(NeuralNetProperties.load(config));
    }
  }

  public static void add(File config, int thread) {
    if (config != null) {
      try {
        add(FileUtils.readFileToString(config, "utf-8"), thread);
      } catch (Throwable t) {
        Log.exception(t);
        throw new RuntimeException(t);
      }
    }
  }

  public static void add(NeuralNet nn, int thread) {
    if (nn != null) {
      net[thread].add(((NeuralNetProperties.Configurable) nn).properties());
    }
  }

  public static void reset(String config, int thread) {
    net[thread] = new NeuralNetVoters();
    if (config != null) {
      net[thread].add(NeuralNetProperties.load(config));
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static void reset(File config, int thread) {
    try {
      reset(FileUtils.readFileToString(config, "utf-8"), thread);
    } catch (Throwable t) {
      Log.exception(t);
      throw new RuntimeException(t);
    }
  }

  public static void reset(NeuralNet nn, int thread) {
    net[thread] = new NeuralNetVoters();
    if (nn != null) {
      net[thread].add(((NeuralNetProperties.Configurable) nn).properties());
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static NeuralNet copy() {
    NeuralNetVoters copy = new NeuralNetVoters();
    copy.add(NeuralNetProperties.load(net.toString()));
    return copy;
  }
}
