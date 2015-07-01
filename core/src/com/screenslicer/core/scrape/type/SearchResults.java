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
package com.screenslicer.core.scrape.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.machinepublishers.browser.Browser;
import com.screenslicer.api.datatype.Result;
import com.screenslicer.api.request.Query;
import com.screenslicer.common.CommonUtil;
import com.screenslicer.core.scrape.ProcessPage;
import com.screenslicer.core.scrape.Scrape.ActionFailed;

public class SearchResults {
  private List<Result> searchResults;
  private List<Result> prevResults;
  private static Collection<SearchResults> instances = new HashSet<SearchResults>();
  private static Object lock = new Object();
  private String window;
  private int page;
  private Query query;
  private static final double MAX_ERROR = .15;

  public static SearchResults newInstance(boolean register) {
    return newInstance(register, null, null, -1, null);
  }

  public static SearchResults newInstance(boolean register,
      List<Result> searchResults, SearchResults source) {
    if (source != null) {
      synchronized (lock) {
        instances.remove(source);
      }
      return newInstance(register, searchResults, source.window, source.page, source.query);
    }
    return newInstance(register, searchResults, null, -1, null);
  }

  public static SearchResults newInstance(boolean register,
      List<Result> searchResults, String window, int page, Query query) {
    SearchResults instance = new SearchResults(searchResults, window, page, query);
    if (register) {
      synchronized (lock) {
        instances.add(instance);
      }
    }
    return instance;
  }

  private SearchResults(List<Result> searchResults, String window, int page, Query query) {
    this.searchResults = searchResults == null ? new ArrayList<Result>()
        : new ArrayList<Result>(searchResults);
    this.prevResults = searchResults == null ? new ArrayList<Result>()
        : new ArrayList<Result>(searchResults);
    this.window = window;
    this.page = page;
    this.query = query;
  }

  public static void revalidate(Browser browser, boolean reset, int thread) {
    Collection<SearchResults> myInstances;
    synchronized (lock) {
      myInstances = new HashSet<SearchResults>(instances);
    }
    if (reset) {
      browser.navigate().refresh();
    }
    for (SearchResults cur : myInstances) {
      if (cur.window != null && cur.query != null && !CommonUtil.isEmpty(cur.prevResults)) {
        List<Result> prevPage = cur.removeLastPage();
        try {
          browser.switchTo().window(cur.window);
          browser.switchTo().defaultContent();
          List<Result> newPage = new ArrayList<Result>(ProcessPage.perform(
              browser, cur.page, cur.query, thread).drain());
          for (int num = newPage.size(); num > prevPage.size(); num--) {
            newPage.remove(num - 1);
          }
          double diff = Math.abs(newPage.size() - prevPage.size());
          if (diff / (double) prevPage.size() > MAX_ERROR) {
            throw new Browser.Fatal();
          }
          cur.prevResults = newPage;
          cur.window = browser.getWindowHandle();
          cur.searchResults.addAll(cur.prevResults);
        } catch (Browser.Fatal f) {
          cur.prevResults = prevPage;
          cur.searchResults.addAll(prevPage);
          throw new Browser.Fatal(f);
        } catch (ActionFailed e) {
          cur.prevResults = prevPage;
          cur.searchResults.addAll(prevPage);
          throw new Browser.Fatal(e);
        }
      }
    }
    String[] handles = browser.getWindowHandles().toArray(new String[0]);
    browser.switchTo().window(handles[handles.length - 1]);
    browser.switchTo().defaultContent();
  }

  private List<Result> removeLastPage() {
    if (!CommonUtil.isEmpty(prevResults)) {
      for (Result toRemove : prevResults) {
        searchResults.remove(toRemove);
      }
      List<Result> lastPage = new ArrayList<Result>(prevResults);
      prevResults.clear();
      return lastPage;
    }
    return new ArrayList<Result>();
  }

  public boolean isEmpty() {
    return CommonUtil.isEmpty(searchResults);
  }

  public int size() {
    return searchResults == null ? 0 : searchResults.size();
  }

  public void remove(int index) {
    if (searchResults != null) {
      if (index < searchResults.size()) {
        Result removed = searchResults.remove(index);
        if (removed != null) {
          prevResults.remove(removed);
        }
      }
    }
  }

  public List<Result> drain() {
    synchronized (lock) {
      instances.remove(this);
    }
    if (prevResults != null) {
      prevResults.clear();
    }
    return searchResults == null ? new ArrayList<Result>() : searchResults;
  }

  public boolean isDuplicatePage(SearchResults newResults) {
    int diff = 0;
    if (newResults != null && prevResults != null && newResults.searchResults != null) {
      if (!prevResults.isEmpty() && prevResults.size() == newResults.searchResults.size()) {
        for (int i = 0; i < prevResults.size(); i++) {
          if (!CommonUtil.equals(prevResults.get(i).summary, newResults.searchResults.get(i).summary)
              || !CommonUtil.equals(prevResults.get(i).title, newResults.searchResults.get(i).title)
              || !CommonUtil.equals(prevResults.get(i).url, newResults.searchResults.get(i).url)) {
            ++diff;
          }
        }
        return (double) diff / (double) prevResults.size() <= MAX_ERROR;
      }
    }
    if (newResults == null || newResults.isEmpty()) {
      return true;
    }
    return false;
  }

  public void addPage(SearchResults newResults) {
    if (newResults != null) {
      if (searchResults == null) {
        searchResults = new ArrayList<Result>();
      }
      List<Result> results = newResults.drain();
      searchResults.addAll(results);
      this.prevResults = new ArrayList<Result>(results);
      this.window = newResults.window;
      this.page = newResults.page;
      this.query = newResults.query;
    }
  }

  public Result get(int index) {
    if (searchResults == null || index >= searchResults.size()) {
      return new Result();
    }
    return searchResults.get(index);
  }

  public Query query() {
    return query;
  }

  public int page() {
    return page;
  }
}
