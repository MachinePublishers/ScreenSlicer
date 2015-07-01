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
package com.screenslicer.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;

public class Config {
  public static Config instance = new Config();

  private final Properties props = new Properties();
  private final String basicAuthUser;
  private final String basicAuthPass;
  private final String transitSecret;
  private final List<String> storageSecrets = new ArrayList<String>();
  private final String mandrillKey;
  private final String mandrillEmail;
  private final String proxy;
  private final String instances;
  private final String myInstance;
  private static final long SLEEP = 1000;

  private Config() {
    File lock = new File("./screenslicer.config.lck");
    while (lock.exists()) {
      try {
        Thread.sleep(SLEEP);
      } catch (Throwable t) {}
    }
    try {
      FileUtils.touch(lock);
    } catch (Throwable t) {
      Log.exception(t);
    }
    File file = new File("./screenslicer.config");
    FileInputStream streamIn = null;
    try {
      FileUtils.touch(file);
      streamIn = new FileInputStream(file);
      props.load(streamIn);
    } catch (Throwable t) {
      Log.exception(t);
    }
    IOUtils.closeQuietly(streamIn);
    basicAuthUser = getAndSet("basic_auth_user", Random.next());
    basicAuthPass = getAndSet("basic_auth_pass", Random.next());
    //for backward compatibility with versions <= 1.0.0
    String secretA = props.getProperty("secret_a");
    String secretB = props.getProperty("secret_b");
    String secretC = props.getProperty("secret_c");
    if (!isEmpty(secretA) && !isEmpty(secretB) && !isEmpty(secretC)) {
      String storageSecret = secretC + secretA + secretB;
      props.setProperty("storage_secret_v1", storageSecret);
      storageSecrets.add(storageSecret);
      props.remove("secret_a");
      props.remove("secret_b");
      props.remove("secret_c");
      props.remove("secret_d");
    } else {
      for (int i = 1; true; i++) {
        String storageSecret = props.getProperty("storage_secret_v" + i);
        if (i == 1 && isEmpty(storageSecret)) {
          storageSecret = Random.next();
          props.setProperty("storage_secret_v1", storageSecret);
          storageSecrets.add(storageSecret);
          break;
        } else if (!isEmpty(storageSecret)) {
          props.setProperty("storage_secret_v" + i, storageSecret);
          storageSecrets.add(storageSecret);
        } else {
          break;
        }
      }
    }
    transitSecret = getAndSet("transit_secret", Random.next());
    mandrillKey = getAndSet("mandrill_key", "");
    mandrillEmail = getAndSet("mandrill_email", "");
    proxy = getAndSet("proxy", "");
    instances = getAndSet("instances", "");
    myInstance = getAndSet("my_instance", "");
    FileOutputStream streamOut = null;
    try {
      streamOut = new FileOutputStream(file);
      props.store(streamOut, null);
    } catch (Throwable t) {
      Log.exception(t);
    }
    IOUtils.closeQuietly(streamOut);
    FileUtils.deleteQuietly(lock);
  }

  private static boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  private String getAndSet(String propertyName, String propertyDefaultValue) {
    String val = props.getProperty(propertyName, propertyDefaultValue);
    props.setProperty(propertyName, val);
    return val;
  }

  public void init() {
    Log.info("screenslicer.config: " + new File("screenslicer.config").getAbsolutePath());
  }

  String transitSecret() {
    return transitSecret;
  }

  int storageSecretVersion() {
    return storageSecrets.size();
  }

  String storageSecret(int version) {
    return storageSecrets.get(version - 1);
  }

  String storageSecret() {
    return storageSecrets.get(storageSecrets.size() - 1);
  }

  public String basicAuthUser() {
    return basicAuthUser;
  }

  public String basicAuthPass() {
    return basicAuthPass;
  }

  public String mandrillKey() {
    return mandrillKey;
  }

  public String mandrillEmail() {
    return mandrillEmail;
  }

  public String proxy() {
    return proxy;
  }

  public String instances() {
    return instances;
  }

  public String myInstance() {
    return myInstance;
  }
}
