/* 
 * ScreenSlicer (TM)
 * Copyright (C) 2013-2015 Machine Publishers, LLC
 * ops@machinepublishers.com | screenslicer.com | machinepublishers.com
 * Cincinnati, Ohio, USA
 *
 * You can redistribute this program and/or modify it under the terms of the GNU Affero General Public
 * License version 3 as published by the Free Software Foundation.
 *
 * jBrowserDriver is made available under the terms of the GNU Affero General Public License version 3
 * with the following clarification and special exception:
 *
 *   Linking jBrowserDriver statically or dynamically with other modules is making a combined work
 *   based on jBrowserDriver. Thus, the terms and conditions of the GNU Affero General Public License
 *   version 3 cover the whole combination.
 *
 *   As a special exception, Machine Publishers, LLC gives you permission to link unmodified versions
 *   of jBrowserDriver with independent modules to produce an executable, regardless of the license
 *   terms of these independent modules, and to copy, distribute, and make available the resulting
 *   executable under terms of your choice, provided that you also meet, for each linked independent
 *   module, the terms and conditions of the license of that module. An independent module is a module
 *   which is not derived from or based on jBrowserDriver. If you modify jBrowserDriver, you may not
 *   extend this exception to your modified version of jBrowserDriver.
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
package com.screenslicer.api.datatype;

import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.screenslicer.common.CommonUtil;

public final class Contact {
  public static final Contact instance(String json) {
    return instance((Map<String, Object>) CommonUtil.gson.fromJson(json, CommonUtil.objectType));
  }

  public static final List<Contact> instances(String json) {
    return instances((List<Map<String, Object>>) CommonUtil.gson.fromJson(json, CommonUtil.listObjectType));
  }

  public static final Contact instance(Map<String, Object> args) {
    return CommonUtil.constructFromMap(Contact.class, args);
  }

  public static final List<Contact> instances(Map<String, Object> args) {
    return CommonUtil.constructListFromMap(Contact.class, args);
  }

  public static final List<Contact> instances(List<Map<String, Object>> args) {
    return CommonUtil.constructListFromMapList(Contact.class, args);
  }

  public static final String toJson(Contact obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<Contact>() {}.getType());
  }

  public static final String toJson(Contact[] obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<Contact[]>() {}.getType());
  }

  public static final String toJson(List<Contact> obj) {
    return CommonUtil.gson.toJson(obj, new TypeToken<List<Contact>>() {}.getType());
  }

  /**
   * Name of a person
   */
  public String name;
  /**
   * Email for a person
   */
  public String email;
  /**
   * Phone number for a person
   */
  public String phone;
}
