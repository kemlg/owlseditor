package org.wonderly.swing;

import javax.swing.*;
import java.util.*;
import java.util.List;

/**
<pre>
Copyright (c) 1997-2006, Gregg Wonderly
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * The name of the author may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>
 *  This is an {@link AbstractListModel} subclass that also implements
 *  the {@link List} interface.  This allows the program to employ
 *  {@link Collections} provided operations such as <code>sort(List)</code>
 *  on the model to manage the ordering of the elements.  It also includes
 *  a {@link #setContents(Vector)} method that will allow the list contents
 *  to be replaced instantaneously so that large list manipulations don't
 *  show the user a bunch of consecutive insert and delete operations
 *  that make the display flash.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 *  @see CircularListModel
 */
public class VectorListModel<T> extends AbstractListModel 
	implements List<T>,ComboBoxModel {

	protected Vector<T> delegate = new Vector<T>();

	public int getSize() {
		return delegate.size();
	}
	
	public VectorListModel( Vector<T> conts ) {
		super();
		delegate = conts;
	}
	
	public VectorListModel() {
	}

	public T getElementAt(int index) {
		if( index >= delegate.size() )
			return null;
		return delegate.elementAt(index);
	}

	public void copyInto(T anArray[]) {
		delegate.copyInto(anArray);
	}

	public void trimToSize() {
		delegate.trimToSize();
	}

	public void ensureCapacity(int minCapacity) {
		delegate.ensureCapacity(minCapacity);
	}

	public void setSize(int newSize) {
		int oldSize = delegate.size();
		delegate.setSize(newSize);
		if (oldSize > newSize) {
			fireIntervalRemoved(this, newSize, oldSize-1);
		}
		else if (oldSize < newSize) {
			fireIntervalAdded(this, oldSize, newSize-1);
		}
	}
	
	public Object getSelectedItem() {
		return comboSel;
	}

	Object comboSel;
	public void setSelectedItem( Object obj ) {
		comboSel = obj;
	}

	public void setContents( Vector<T> v ) {
		delegate = v;
		fireContentsChanged( this, 0, v.size()-1);
	}

	public Vector<T> getContents() {
		return delegate;
	}

	public int capacity() {
		return delegate.capacity();
	}

	public int size() {
		return delegate.size();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Enumeration<T> elements() {
		return delegate.elements();
	}

	public boolean contains(Object elem) {
		return delegate.contains(elem);
	}

	public int indexOf(Object elem) {
		return delegate.indexOf(elem);
	}

	public int indexOf(T elem, int index) {
		return delegate.indexOf(elem, index);
	}

	public int lastIndexOf(Object elem) {
		return delegate.lastIndexOf(elem);
	}

	public int lastIndexOf(T elem, int index) {
		return delegate.lastIndexOf(elem, index);
	}

	public T elementAt(int index) {
		return delegate.elementAt(index);
	}

	public T firstElement() {
		return delegate.firstElement();
	}

	public T lastElement() {
		return delegate.lastElement();
	}

	public void setElementAt(T obj, int index) {
		delegate.setElementAt(obj, index);
		fireContentsChanged(this, index, index);
	}

	public void removeElementAt(int index) {
		delegate.removeElementAt(index);
		fireIntervalRemoved(this, index, index);
	}

	public void insertElementAt(T obj, int index) {
		delegate.insertElementAt(obj, index);
		fireIntervalAdded(this, index, index);
	}

	public void addElement(T obj) {
		int index = delegate.size();
		delegate.addElement(obj);
		fireIntervalAdded(this, index, index);
	}

	public boolean removeElement(T obj) {
		int index = indexOf(obj);
		boolean rv = delegate.removeElement(obj);
		if (index > 0) {
			fireIntervalRemoved(this, index, index);
		}
		return rv;
	}


	public void removeAllElements() {
		int index1 = delegate.size()-1;
		delegate.removeAllElements();
		if (index1 >= 0) {
			fireIntervalRemoved(this, 0, index1);
		}
	}

	public Object[] toArray() {
		Object[] rv = new Object[delegate.size()];
		delegate.copyInto(rv);
		return rv;
	}

	public T get(int index) {
		return delegate.elementAt(index);
	}

	public T set(int index, T element) {
		T rv = delegate.elementAt(index);
		delegate.setElementAt(element, index);
		fireContentsChanged(this, index, index);
		return rv;
	}

	public void add(int index, T element) {
		delegate.insertElementAt(element, index);
		fireIntervalAdded(this, index, index);
	}

	public T remove(int index) {
		T rv = delegate.elementAt(index);
		delegate.removeElementAt(index);
		fireIntervalRemoved(this, index, index);
		return rv;
	}

	public void clear() {
		int index1 = delegate.size()-1;
		delegate.removeAllElements();
		if (index1 >= 0) {
			fireIntervalRemoved(this, 0, index1);
		}
	}

	public void removeRange(int fromIndex, int toIndex) {
		for(int i = toIndex; i >= fromIndex; i--) {
			delegate.removeElementAt(i);
		}
		fireIntervalRemoved(this, fromIndex, toIndex);
	}
	public String toString() {
		return delegate.toString();
	}
	// List implementation follows...
//	public void add(int index, Object element) {
//		delegate.add( index, element );
//	}
	public boolean add(T o) {
		return delegate.add(o);
	}
	public boolean addAll(Collection<? extends T> c) {
		return delegate.addAll(c);
	}
	public boolean addAll(int index, Collection<? extends T> c) {
		return delegate.addAll( index, c );
	}
//	public void clear() {
//		delegate.clear();
//	}
//	public boolean contains(Object o) {
//		return delegate.contains(o);
//	}
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}
	public boolean equals(Object o) {
		return delegate.equals(o);
	}
//	public Object get(int index) {
//		return delegate.get(index);
//	}
	public int hashCode() {
		return delegate.hashCode();
	}
//	public int indexOf(T o) {
//		return delegate.indexOf(o);
//	}
//	public boolean isEmpty() {
//		return delegate.isEmpty();
//	}
	public Iterator<T> iterator() {
		return delegate.iterator();
	}
//	public int lastIndexOf(Object o) {
//		return delegate.lastIndexOf(o);
//	}
	public ListIterator<T> listIterator() {
		return delegate.listIterator();
	}
	public ListIterator<T> listIterator(int index) {
		return delegate.listIterator( index );
	}
//	public Object remove(int index) {
//		return delegate.remove(index);
//	}
	public boolean remove(Object o) {
		return delegate.remove(o);
	}
	public boolean removeAll(Collection<?> c) {
		return delegate.removeAll(c);
	}
	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll(c);
	}
//	public Object set(int index, Object element) {
//		return delegate.set(index,element);
//	}
//	public int size() {
//		return delegate.size();
//	}
	public List<T> subList(int fromIndex, int toIndex) {
		return delegate.subList( fromIndex, toIndex );
	}
//	public Object[] toArray() {
//		return delegate.toArray();
//	}
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}
}
