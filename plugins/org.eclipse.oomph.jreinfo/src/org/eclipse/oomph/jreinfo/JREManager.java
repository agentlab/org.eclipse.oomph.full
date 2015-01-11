/*
 * Copyright (c) 2014 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.oomph.jreinfo;

import org.eclipse.oomph.internal.jreinfo.JREInfoPlugin;
import org.eclipse.oomph.util.IOUtil;
import org.eclipse.oomph.util.OS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Eike Stepper
 */
public final class JREManager
{
  public static final OSType OS_TYPE = determineOSType();

  public static final int BITNESS = determineBitness();

  public static final boolean BITNESS_CHANGEABLE = BITNESS == 64 && OS.INSTANCE.is32BitAvailable();

  public static final String JAVA_EXECUTABLE = OS_TYPE == OSType.Win ? "java.exe" : "java";

  public static final String JAVA_COMPILER = OS_TYPE == OSType.Win ? "javac.exe" : "javac";

  public static final JREManager INSTANCE = new JREManager();

  private final List<String> javaHomes = new ArrayList<String>();

  private JREManager()
  {
    loadJavaHomes();
  }

  private void addExtraJavaHomes(List<String> extraJavaHomes, File folder, boolean root, Set<JRE> result, IProgressMonitor monitor)
  {
    JREInfoPlugin.checkCancelation(monitor);
    String path = folder.getAbsolutePath();

    File[] childFolders = folder.listFiles(new FileFilter()
    {
      public boolean accept(File pathname)
      {
        return pathname.isDirectory();
      }
    });

    try
    {
      int children = childFolders == null ? 0 : childFolders.length;
      monitor.beginTask(root ? "Searching for VMs in " + path + "..." : "", 1 + children);
      monitor.subTask(path);

      if (!javaHomes.contains(path) && !extraJavaHomes.contains(path))
      {
        File executable = new File(folder, "bin/" + JAVA_EXECUTABLE);
        if (executable.isFile())
        {
          File canonicalFolder = folder.getCanonicalFile();

          JRE info = InfoManager.INSTANCE.getInfo(canonicalFolder);
          if (info != null && info.isValid())
          {
            extraJavaHomes.add(path);
            result.add(new JRE(folder, info));
          }
        }
      }

      monitor.worked(1);

      for (int i = 0; i < children; i++)
      {
        addExtraJavaHomes(extraJavaHomes, childFolders[i], false, result, new SubProgressMonitor(monitor, 1));
      }
    }
    catch (OperationCanceledException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      //$FALL-THROUGH$
    }
    finally
    {
      monitor.done();
    }
  }

  public synchronized Set<JRE> addExtraJavaHomes(String rootFolder, IProgressMonitor monitor)
  {
    Set<JRE> result = new HashSet<JRE>();

    File folder = new File(rootFolder);
    if (folder.isDirectory())
    {
      List<String> extraJavaHomes = loadExtraJavaHomes();
      addExtraJavaHomes(extraJavaHomes, folder, true, result, monitor);

      if (!result.isEmpty())
      {
        saveExtraJavaHomes(extraJavaHomes);
      }
    }

    return result;
  }

  public synchronized void removeExtraJavaHomes(String... javaHomes)
  {
    List<String> extraJavaHomes = loadExtraJavaHomes();
    if (extraJavaHomes.removeAll(Arrays.asList(javaHomes)))
    {
      saveExtraJavaHomes(extraJavaHomes);
    }
  }

  public synchronized void refresh(boolean refreshInfos)
  {
    if (refreshInfos)
    {
      InfoManager.INSTANCE.refresh();
    }

    loadJavaHomes();
  }

  public LinkedHashMap<File, JRE> getJREs()
  {
    return getJREs(null);
  }

  public LinkedHashMap<File, JRE> getJREs(JREFilter filter)
  {
    Set<File> folders = getJavaHomes();
    List<JRE> jres = getJREs(filter, folders);

    LinkedHashMap<File, JRE> result = new LinkedHashMap<File, JRE>();
    for (JRE jre : jres)
    {
      result.put(jre.getJavaHome(), jre);
    }

    return result;
  }

  public JRE[] getJREs(JREFilter filter, boolean extra)
  {
    Set<File> folders = toFile(extra ? loadExtraJavaHomes() : javaHomes);
    List<JRE> jres = getJREs(filter, folders);
    return jres.toArray(new JRE[jres.size()]);
  }

  private synchronized Set<File> getJavaHomes()
  {
    Set<File> all = new HashSet<File>();
    all.addAll(toFile(javaHomes));
    all.addAll(toFile(loadExtraJavaHomes()));
    return all;
  }

  private void loadJavaHomes()
  {
    javaHomes.clear();

    JREInfo info = JREInfo.getAll();
    while (info != null)
    {
      javaHomes.add(info.javaHome);
      info = info.next;
    }
  }

  private static List<String> loadExtraJavaHomes()
  {
    if (getCacheFile().isFile())
    {
      try
      {
        return IOUtil.readLines(getCacheFile(), "UTF-8");
      }
      catch (Exception ex)
      {
        JREInfoPlugin.INSTANCE.log(ex);
      }
    }

    return new ArrayList<String>();
  }

  private static void saveExtraJavaHomes(List<String> paths)
  {
    try
    {
      IOUtil.writeLines(getCacheFile(), "UTF-8", paths);
    }
    catch (Exception ex)
    {
      JREInfoPlugin.INSTANCE.log(ex);
    }
  }

  private static File getCacheFile()
  {
    return new File(JREInfoPlugin.INSTANCE.getUserLocation().append("extra.txt").toOSString());
  }

  private static List<JRE> getJREs(JREFilter filter, Collection<File> javaHomes)
  {
    List<JRE> list = new ArrayList<JRE>();
    for (File javaHome : javaHomes)
    {
      try
      {
        File canonicalJavaHome = javaHome.getCanonicalFile();
        JRE info = InfoManager.INSTANCE.getInfo(canonicalJavaHome);
        if (info != null && info.isValid())
        {
          if (filter == null || info.isMatch(filter))
          {
            list.add(new JRE(javaHome, info));
          }
        }
      }
      catch (IOException ex)
      {
        JREInfoPlugin.INSTANCE.log(ex);
      }
    }

    Collections.sort(list);
    return list;
  }

  private static Set<File> toFile(Collection<String> paths)
  {
    Set<File> result = new HashSet<File>();
    for (String javaHome : paths)
    {
      result.add(new File(javaHome));
    }

    return result;
  }

  private static OSType determineOSType()
  {
    String os = Platform.getOS();
  
    if (Platform.OS_WIN32.equals(os))
    {
      System.loadLibrary("jreinfo.dll");
      return OSType.Win;
    }
  
    if (Platform.OS_MACOSX.equals(os))
    {
      return OSType.Mac;
    }
  
    if (Platform.OS_LINUX.equals(os))
    {
      return OSType.Linux;
    }
  
    return OSType.Unsupported;
  }

  private static int determineBitness()
  {
    try
    {
      return org.eclipse.oomph.extractor.lib.JRE.determineBitness();
    }
    catch (Throwable ex)
    {
      JREInfoPlugin.INSTANCE.log(ex);
      return 32;
    }
  }
}
