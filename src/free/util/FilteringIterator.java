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

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An implementation of the <code>Enumeration</code> interface which delegates
 * to another <code>Enumeration</code>, but only returns elements which pass
 * the {@link #accept(Object)} method.
 */

public abstract class FilteringIterator implements Iterator{



  /**
   * The delegate enumeration.
   */

  private final Iterator delegate;



  /**
   * The next element we'll return. This is set by the <code>findNext</code>
   * method.
   */

  private Object next = null;



  /**
   * Creates a new <code>FilteringEnumeration</code> object with the specified
   * delegate.
   */

  public FilteringIterator(Iterator delegate){
    this.delegate = delegate;
  }



  /**
   * Finds the next element in the delegate enumeration which passes
   * <code>accept</code> and puts it in <code>next</code>.
   */

  private void findNext(){
    if (next != null)
      return;

    while (delegate.hasNext()){
      Object element = delegate.next();
      if (accept(element)){
        next = element;
        break;
      }
    }
  }



  /**
   * Returns whether there are more elements in this <code>Iterator</code>.
   */

  public boolean hasNext(){
    findNext();

    return next != null;
  }



  /**
   * Returns the next element in the delegate iterator which passes the
   * <code>accept</code> method.
   */

  public Object next() throws NoSuchElementException{
    findNext();

    if (next == null)
      throw new NoSuchElementException();

    Object result = next;
    next = null;
    return result;
  }

  /**
   * Removes the next element in the delegate iterator.
   */
  @Override
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns whether the specified object passes the filter.
   */

  public abstract boolean accept(Object element);



}