/* 
 * ScreenSlicer (TM) -- automatic, zero-config web scraping (TM)
 * Copyright (C) 2013-2015 Machine Publishers, LLC
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
package com.screenslicer.api.request;

import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.screenslicer.common.CommonUtil;

public final class Cancel {
  public static final Cancel instance(String json) {
    return instance((Map<String, Object>) CommonUtil.gson.fromJson(json, CommonUtil.objectType));
  }

  public static final List<Cancel> instances(String json) {
    return instances((List<Map<String, Object>>) CommonUtil.gson.fromJson(json, CommonUtil.listObjectType));
  }

  public static final Cancel instance(Map<String, Object> args) {
    return CommonUtil.constructFromMap(Cancel.class, args);
  }

  public static final List<Cancel> instances(Map<String, Object> args) {
    return CommonUtil.constructListFromMap(Cancel.class, args);
  }

  public static final List<Cancel> instances(List<Map<String, Object>> args) {
    return CommonUtil.constructListFromMapList(Cancel.class, args);
  }

  public static final String toJson(Cancel obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<Cancel>() {}.getType());
  }

  public static final String toJson(Cancel[] obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<Cancel[]>() {}.getType());
  }

  public static final String toJson(List<Cancel> obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<List<Cancel>>() {}.getType());
  }

  /**
   * IP addresses of ScreenSlicer instances
   */
  public String[] instances;
  /**
   * GUID of the run to cancel
   */
  public String runGuid;
}
