/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <P>A blocking queue, one that always "contains" elements.
 * If it is in fact empty, the pop() and peek() method will block until an
 * item is pushed.
 * <P><B>NOTE:</B> This class is thread safe.
 *
 * @author Alexander Maryanovsky.
 */

public class BlockingQueue implements Cloneable{


  /**
   * The underlying ArrayList this BlockingQueue is using.
   */

  private final ArrayList queue;


  /**
   * The lock we use to synchronize pushing.
   */

  private final Object pushLock = new String("BlockingQueue pushLock");


  /**
   * The lock we use to synchronize popping.
   */

  private final Object popLock = new String("BlockingQueue popLock");




  /**
   * Creates a new, empty BlockingQueue.
   */

  public BlockingQueue(){
    queue = new ArrayList();
  }





  /**
   * Pushes an element into the queue.
   */

  public void push(Object object){
    synchronized(pushLock){
      queue.add(object);
      synchronized(this){
        notify();
      }
    }
  }



  /**
   * Pops an element from the queue. If the queue is empty, this method blocks
   * until another thread pushes an element into the queue.
   *
   * @throws InterruptedException if the invoking thread was interrupted
   * while waiting for an element to be pushed into the queue.
   */

  public Object pop() throws InterruptedException{
    return pop(0);
  }



  /**
   * Pops an element from the queue. Unlike the pop() method, this method does not block
   * for longer than the given amount of milliseconds. When the given amount of milliseconds
   * have passed, this method will throw an InterruptedException.
   */

  public Object pop(long timeout) throws InterruptedException{
    synchronized(popLock){
      synchronized(this){
        if (queue.isEmpty()){
          wait(timeout);
          if (queue.isEmpty())
            throw new InterruptedException("Timed out");
        }
      }
      Object val = queue.get(0);
      queue.remove(0);
      return val;
    }
  }



  /**
   * Returns the element on top of the queue without popping it. If the queue
   * is empty, this method blocks until another thread pushes an element into
   * the queue.
   *
   * @throws InterruptedException if the invoking thread was interrupted while
   * waiting for an element to be pushed into the queue.
   */

  public Object peek() throws InterruptedException{
    return peek(0);
  }




  /**
   * Returns the element on top of the queue without popping it.
   * Unlike the peek() method, this method does not block
   * for longer than the given amount of milliseconds. When the given amount of milliseconds
   * have passed, this method will throw an InterruptedException.
   */

  public Object peek(long timeout) throws InterruptedException{
    synchronized(popLock){
      synchronized(this){
        if (queue.isEmpty()){
          wait(timeout);
          if (queue.isEmpty())
            throw new InterruptedException("Timed out");
        }
      }
      return queue.get(0);
    }
  }






  /**
   * Returns true if the queue is empty (this returns the actual state of the
   * queue, meaning it may return true even though ideologically, a BlockingQueue
   * is never empty).
   */

  public boolean isEmpty(){
    return queue.isEmpty();
  }



  /**
   * Returns true if the given element is in the queue.
   */

  public boolean contains(Object element){
    return queue.contains(element);
  }



  /**
   * Returns the size of the queue.
   */

  public int size(){
    return queue.size();
  }



  /**
   * Returns an Iterator of the elements in this queue. The order of the
   * elements is the same as if they were popped from the queue one by one (the
   * first element is the first element that would have been popped). <br>
   * <B>IMPORTANT:</B> Modifying the queue breaks the returned Iterator.
   */

  public Iterator getElements(){
    return queue.iterator();
  }



  /**
   * Removes all elements from this queue.
   */

  public void removeAllElements(){
    queue.clear();
  }



  /**
   * Removes all elements from this queue.
   */

  public void clear(){
    queue.clear();
  }



  /**
   * Returns a shallow copy of this BlockingQueue.
   */

  public synchronized Object clone(){
    BlockingQueue copy = new BlockingQueue();
    Iterator elems = getElements();
    while (elems.hasNext()){
      Object item = elems.next();
      copy.push(item);
    }
    return copy;
  }



}
