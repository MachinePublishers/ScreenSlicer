/* 
 * ScreenSlicer (TM) -- automatic, zero-config web scraping (TM)
 * Copyright (C) 2013-2014 Machine Publishers, LLC
 * ops@machinepublishers.com | screenslicer.com | machinepublishers.com
 * 717 Martin Luther King Dr W Ste I, Cincinnati, Ohio 45220
 *
 * You can redistribute this program and/or modify it under the terms of the
 * GNU Affero General Public License version 3 as published by the Free
 * Software Foundation. Additional permissions or commercial licensing may be
 * available--see LICENSE file or contact Machine Publishers, LLC for details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License version 3
 * for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * version 3 along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For general details about how to investigate and report license violations,
 * please see: https://www.gnu.org/licenses/gpl-violation.html
 * and email the author: ops@machinepublishers.com
 * Keep in mind that paying customers have more rights than the AGPL alone offers.
 */
package com.screenslicer.core.scrape.type;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.openqa.selenium.remote.BrowserDriver;

import com.screenslicer.api.datatype.SearchResult;
import com.screenslicer.api.request.Query;
import com.screenslicer.common.CommonUtil;
import com.screenslicer.common.Log;
import com.screenslicer.core.scrape.ProcessPage;
import com.screenslicer.core.scrape.Scrape.ActionFailed;
import com.screenslicer.core.util.Util;

public class SearchResults {
  private List<SearchResult> searchResults;
  private List<SearchResult> prevResults;
  private static Collection<SearchResults> instances = new HashSet<SearchResults>();
  private static Object lock = new Object();
  private String window;
  private int page;
  private Query query;

  public static SearchResults newInstance() {
    return newInstance(null, null, -1, null);
  }

  public static SearchResults newInstance(
      List<SearchResult> searchResults, SearchResults source) {
    if (source != null) {
      synchronized (lock) {
        instances.remove(source);
      }
      return newInstance(searchResults, source.window, source.page, source.query);
    }
    return newInstance(searchResults, null, -1, null);
  }

  public static SearchResults newInstance(
      List<SearchResult> searchResults, String window, int page, Query query) {
    SearchResults instance = new SearchResults(searchResults, window, page, query);
    synchronized (lock) {
      instances.add(instance);
    }
    return instance;
  }

  private SearchResults(List<SearchResult> searchResults, String window, int page, Query query) {
    this.searchResults = searchResults == null ? new ArrayList<SearchResult>()
        : new ArrayList<SearchResult>(searchResults);
    this.prevResults = searchResults == null ? new ArrayList<SearchResult>()
        : new ArrayList<SearchResult>(searchResults);
    this.window = window;
    this.page = page;
    this.query = query;
  }

  public static void revalidate(BrowserDriver driver) {
    Collection<SearchResults> myInstances;
    synchronized (lock) {
      myInstances = new HashSet<SearchResults>(instances);
    }
    driver.reset();
    Util.driverSleepReset();
    for (SearchResults cur : myInstances) {
      try {
        if (cur.window != null && cur.query != null && !CommonUtil.isEmpty(cur.prevResults)) {
          int size = cur.removeLastPage();
          try {
            driver.switchTo().window(cur.window);
            driver.switchTo().defaultContent();
            cur.prevResults = new ArrayList<SearchResult>(ProcessPage.perform(driver, cur.page, cur.query).drain());
            int newSize = cur.prevResults.size();
            for (int num = newSize; num > size; num--) {
              cur.prevResults.remove(num - 1);
            }
            cur.window = driver.getWindowHandle();
            cur.searchResults.addAll(cur.prevResults);
          } catch (ActionFailed e) {
            Log.exception(e);
          }
        }
      } catch (Throwable t) {
        Log.exception(t);
      }
    }
    String[] handles = driver.getWindowHandles().toArray(new String[0]);
    driver.switchTo().window(handles[handles.length - 1]);
    driver.switchTo().defaultContent();
    if (handles.length > 1) {
      try {
        new URL(driver.getCurrentUrl());
      } catch (Throwable t) {
        try {
          driver.close();
        } catch (Throwable t2) {
          Log.exception(t2);
        }
        driver.switchTo().window(handles[handles.length - 2]);
        driver.switchTo().defaultContent();
      }
    }
  }

  private int removeLastPage() {
    if (!CommonUtil.isEmpty(prevResults)) {
      for (SearchResult toRemove : prevResults) {
        searchResults.remove(toRemove);
      }
      int size = prevResults.size();
      prevResults.clear();
      return size;
    }
    return 0;
  }

  public boolean isEmpty() {
    return searchResults.isEmpty();
  }

  public int size() {
    return searchResults.size();
  }

  public void remove(int index) {
    if (index < searchResults.size()) {
      searchResults.remove(index);
    }
  }

  public List<SearchResult> drain() {
    synchronized (lock) {
      instances.remove(this);
    }
    prevResults.clear();
    return searchResults;
  }

  public void addPage(SearchResults newResults) {
    List<SearchResult> results = newResults.drain();
    searchResults.addAll(results);
    this.prevResults = new ArrayList<SearchResult>(results);
    this.window = newResults.window;
    this.page = newResults.page;
    this.query = newResults.query;
  }

  public SearchResult get(int index) {
    if (index >= searchResults.size()) {
      return new SearchResult();
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
