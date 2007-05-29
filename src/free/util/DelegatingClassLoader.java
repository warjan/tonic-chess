/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * A class loader which delegates loading to other classloaders.
 */

public class DelegatingClassLoader extends ChildClassLoader{



  /**
   * A list of <code>ChildClassLoaders</code> to which we delegate.
   */

  private final ArrayList delegates = new ArrayList();



  /**
   * The set of class/resource names we are currently in the process of loading.
   * This is needed to avoid endless recursion in case of delegate
   * ChildClassLoaders that have this classloader as their parent.
   */

  private final HashMap beingLoaded = new HashMap();



  /**
   * Creates a new <code>DelegatingClassLoader</code> with the specified parent
   * class loader.
   */

  public DelegatingClassLoader(ChildClassLoader parent){
    super(parent);
  }



  /**
   * Creates a new <code>DelegatingClassLoader</code> with no parent.
   */

  public DelegatingClassLoader(){
    super();
  }



  /**
   * Adds a delegate class loader.
   */

  public void addDelegate(ChildClassLoader classLoader){
    delegates.add(classLoader);
  }



  /**
   * Removes the specified delegate class loader.
   */

  public void removeDelegate(ChildClassLoader classLoader){
    delegates.remove(classLoader);
  }



  /**
   * Loads and optionally resolves the specified class.
   */

  protected Class loadClassImpl(String name, boolean resolve){
    if (beingLoaded.containsKey(name)) // Avoid endless recursion
      return null;

    beingLoaded.put(name, name);
    Class c = null;
    Iterator loaders = delegates.iterator();
    while (loaders.hasNext()){
      ChildClassLoader loader = (ChildClassLoader)loaders.next();
      try{
        c = loader.loadClass(name, resolve);
        if (c != null)
          break;
      } catch (ClassNotFoundException e){}
    }

    beingLoaded.remove(name);
    return c;
  }



  /**
   * Returns an <code>InputStream</code> for reading the resource with the
   * specified name.
   */

  protected InputStream getResourceAsStreamImpl(String name){
    if (beingLoaded.containsKey(name)) // Avoid endless recursion
      return null;

    beingLoaded.put(name, name);
    InputStream in = null;
    Iterator loaders = delegates.iterator();
    while (loaders.hasNext()){
      ChildClassLoader loader = (ChildClassLoader)loaders.next();
      in = loader.getResourceAsStream(name);
      if (in != null)
        break;
    }

    beingLoaded.remove(name);
    return in;
  }



  /**
   * Returns a <code>URL</code> pointing to the resource with the specified
   * name.
   */

  protected URL getResourceImpl(String name){
    if (beingLoaded.containsKey(name)) // Avoid endless recursion
      return null;

    beingLoaded.put(name, name);
    URL url = null;
    Iterator loaders = delegates.iterator();
    while (loaders.hasNext()){
      ChildClassLoader loader = (ChildClassLoader)loaders.next();
      url = loader.getResource(name);
      if (url != null)
        break;
    }

    beingLoaded.remove(name);
    return url;
  }



}