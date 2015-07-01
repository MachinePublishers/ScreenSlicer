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
package com.screenslicer.core.scrape.trainer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.screenslicer.common.CommonUtil;
import com.screenslicer.core.scrape.Proceed;

public class TrainerVisitorProceed implements TrainerProceed.Visitor {
  private final ArrayList<String> nextButtons = new ArrayList<String>();
  private final ArrayList<Element> elements = new ArrayList<Element>();
  private String[] names;

  @Override
  public void init() {
    final ArrayList<String> filenames = new ArrayList<String>();
    final List<String> bump = Arrays.asList(new String[] {
        "buzzfeed"
    });
    new File(System.getProperty("screenslicer.testdata")).listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        if (!file.getAbsolutePath().endsWith("-success")
            && !file.getAbsolutePath().endsWith("-successnode")
            && !file.getAbsolutePath().endsWith("-result")
            && !file.getAbsolutePath().endsWith("-num")
            && !file.getAbsolutePath().endsWith("-next")) {
          try {
            File fileNext = new File(file.getAbsolutePath() + "-next");
            if (fileNext.exists()) {
              if (bump.contains(file.getName())) {
                nextButtons.add(0, FileUtils.readFileToString(fileNext, "utf-8"));
                elements.add(0, DataUtil.load(file, "utf-8", "http://localhost").body());
                filenames.add(0, file.getName());
              } else {
                nextButtons.add(FileUtils.readFileToString(fileNext, "utf-8"));
                elements.add(DataUtil.load(file, "utf-8", "http://localhost").body());
                filenames.add(file.getName());
              }
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        return false;
      }
    });
    for (String filename : filenames) {
      System.out.println(filename);
    }
    names = filenames.toArray(new String[0]);
  }

  @Override
  public int visit(int curTrainingData) {
    int result = 0;
    if (!nextButtons.get(curTrainingData).equals("unknown")) {
      Node next = Proceed.perform(elements.get(curTrainingData), 2).node;
      if (next == null && nextButtons.get(curTrainingData).equals("n/a")) {
        System.out.println("pass - " + names[curTrainingData]);
      } else if (next != null
          && CommonUtil.strip(next.outerHtml(), false).replace(" ", "")
              .startsWith(CommonUtil.strip(nextButtons.get(curTrainingData), false).replace(" ", ""))) {
        System.out.println("pass - " + names[curTrainingData]);
      } else {
        System.out.println("fail - " + names[curTrainingData]);
        if (next != null) {
          System.out.println("Actual--" + CommonUtil.strip(next.outerHtml(), false));
        }
        System.out.println("Expected--" + CommonUtil.strip(nextButtons.get(curTrainingData), false));
        result = 1;
      }
    }
    return result;
  }

  @Override
  public int trainingDataSize() {
    return nextButtons.size();
  }
}
