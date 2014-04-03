/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */
package entropy.vjob.queue;

import java.io.File;
import java.io.IOException;
import java.util.*;

import entropy.vjob.VJob;
import entropy.vjob.builder.DefaultVJobFileSerializerFactory;
import entropy.vjob.builder.VJobBuilderException;
import entropy.vjob.builder.VJobBuilderFactory;

/** A simple storable queue. All the vjobs are stored into a folder into a given
 * format ({@value #DEFAULT_EXTENSION} by default). VJob can be added (removed)
 * either using the add(remove) method, or by putting(deleting) the vjob in the
 * folder. VJobs can be in any format supported by the VJobBuilderFactory.
 * @author Fabien Hermenier */
public class FCFSPersistentQueue extends VJobsPool {

  /** Compare the file by their modification date. Older first */
  private static Comparator<File> olderFirst = new Comparator<File>() {

    @Override
    public int compare(File f1, File f2) {
      return (int) (f1.lastModified() - f2.lastModified());
    }
  };

  /** The current directory that store VJobs. */
  private File rootDir = new File("./vjobs");

  /** The extension of the file. */
  public static final String DEFAULT_EXTENSION = "xml";

  private String extension = DEFAULT_EXTENSION;
  /** The builder to create the VJob from a file. */
  private VJobBuilderFactory builder;

  /** Make a new queue. If the folder describing the queue does not exists, it is
   * created.
   * @param b the builder that constructs VJobs
   * @param folder the folder where to store the vjobs. */
  public FCFSPersistentQueue(VJobBuilderFactory b, File folder) {
    builder = b;
    rootDir = folder;
    if (!rootDir.exists() && !rootDir.mkdirs()) {
      getLogger().info("Unable to create folder '" + folder + "'");
    }
  }

  public void setFileExtension(String ext) {
    extension = ext;
  }

  public String getFileExtension() {
    return extension;
  }

  /** Get the VJobs by browsing the folder.
   * @return a list of vjobs, may be empty */
  @Override
  public List<VJob> getRunningPriorities() {
    List<VJob> readed = new LinkedList<VJob>();
    List<File> files = new ArrayList<File>();
    for (File f : rootDir.listFiles()) {
      if (f.getName().endsWith(extension)) {
        files.add(f);
      } else {
        getLogger().debug("Ignoring '" + f.getName() + "'");
      }
    }
    Collections.sort(files, olderFirst);
    for (File f : files) {
      try {
        VJob l = builder.build(f.getPath());
        readed.add(l);
      }
      catch (IOException e) {
        getLogger().debug(
            "Skipping vJob in " + f.getName() + ": " + e.getMessage());
      }
      catch (VJobBuilderException e) {
        getLogger().debug(
            "Skipping vJob in " + f.getName() + ": " + e.getMessage());
      }
    }
    return readed;
  }

  /** Put the VJob in the queue folder. TODO: Fix where it already exists a vjob
   * with the same id BUT with a different format (so extension)
   * @param v the vjob to add
   * @return true if the vjob is stored into the queue folder. */
  @Override
  public boolean add(VJob v) {
    try {
      DefaultVJobFileSerializerFactory.getInstance().write(v,
          new File(buildPath(v.id())).getPath());
    }
    catch (IOException e) {
      getLogger().warn(e.getMessage());
      return false;
    }
    return true;
  }

  /** Remove the VJob from the queue folder.
   * @param v the vjob to remove
   * @return true if the vjob was deleted */
  @Override
  public boolean remove(VJob v) {
    File f = new File(buildPath(v.id()));
    return f.exists() && f.delete();
  }

  @Override
  public VJob get(String id) {
    File f = new File(buildPath(id));
    if (!f.exists()) {
      return null;
    }
    try {
      return builder.build(f.getPath());
    }
    catch (IOException e) {
      getLogger().debug(
          "Ignoring vJob in " + f.getName() + ": " + e.getMessage());
      return null;
    }
    catch (VJobBuilderException e) {
      getLogger().debug(
          "Ignoring vJob in " + f.getName() + ": " + e.getMessage());
      return null;
    }

  }

  /** Build the absolute path of the vjob.
   * @param id the identifier of the vjob
   * @return the absolute path */
  private String buildPath(String id) {
    StringBuilder b = new StringBuilder();
    b.append(rootDir.getAbsolutePath());
    b.append(File.separator);
    b.append(id).append('.');
    b.append(extension);
    return b.toString();

  }

  // /** Remove the extension of a file name
  // * @param f the file
  // * @return the name */
  // private String removeExtension(File f) {
  // String name = f.getName();
  // return name.substring(0, name.lastIndexOf('.'));
  // }

  /** Get the directory used to store the vjobs description.
   * @return an existing folder */
  public File getFolder() {
    return rootDir;
  }
}
