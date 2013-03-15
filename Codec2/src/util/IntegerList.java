package util;


import java.util.Arrays;


public class IntegerList {
	private int[] data;
	protected int size, capacity;
	
	public IntegerList(IntegerList list) {
		data = General.copy(list.data);
		size = list.size;
		capacity = list.capacity;
	}
	
	/**
	 * Initialize a list with a capacity of at least minimumCapacity
	 */
	public IntegerList(int minimumCapacity) {
		if(minimumCapacity < 1)
			minimumCapacity = 1;
		data = new int[minimumCapacity];
		size = 0;
		capacity = minimumCapacity;
	}
	
	/**
	 * Initialize a list with a capacity of 1
	 */
	public IntegerList() {
		this(1);
	}
	
	/**
	 * Initialize a list with the given array
	 */
	public IntegerList(int[] data) {
		this.data = data;
		size = data.length;
		capacity = size;
	}
	
	/**
	 * Returns the object at the specified index
	 */
	public int get(int index) {
		if(index >= size)
			throw new ArrayIndexOutOfBoundsException();
		return data[index];
	}
	
	/**
	 * Sets the specified index to the specified object.
	 * Returns true if successful.
	 * Returns false if the index is not in the range 0 ~ (size - 1)
	 */
	public boolean set(int object, int index) {
		if(index > -1 && index < size) {
			data[index] = object;
			return true;
		}
		else return false;
	}
	
	/**
	 * Appends the specified object to the end of the list
	 */
	public void add(int object) {
		if(size == capacity)
			increaseCapacity(capacity*2);
		
		data[size] = object;
		size++;
	}
	
	/**
	 * Adds the specified object to the specified index.
	 * Enlarges the array if necessary
	 */
	public void add(int object, int index) {
		if(index >= capacity) {
			increaseCapacity( (1 + index/capacity)*capacity );
		}
		data[index] = object;
		if(index > size)
			size = index + 1;
	}
	
	public int remove(int index) {
		size--;
		int value = data[index];
		for(int i = index; i < size; i++) {
			data[i] = data[i + 1];
		}
		return value;
	}
	
	/**
	 * Removes the first occurrence of <code>element</code>
	 * Returns true if the element is found
	 */
	public boolean removeElement(int element) {
		for(int i = 0; i < data.length; i++) {
			if(data[i] == element) {
				remove(i);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Removes all elements from <code>startIndex</code> onward, inclusive
	 */
	public void removeFrom(int startIndex) {
		size = startIndex;
	}
	
	/**
	 * Returns the size of the list
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Returns the capacity of the list
	 */
	public int capacity() {
		return capacity;
	}
	
	/**
	 * Increase the capacity of the list to newCapacity
	 */
	public void increaseCapacity(int newCapacity) {
		
		// copy old data over
		int[] newData = new int[newCapacity];
		for(int i = 0; i < capacity; i++) {
			newData[i] = data[i];
		}
		
		// update pointer
		data = newData;
		
		// update capacity
		capacity = newCapacity;
	}
	
	/**
	 * Returns the array representation of the list
	 */
	public int[] toArray() {
		return data;
		
	}
	
	/**
	 * Returns the array representation of the list, trimmed to size
	 */
	public int[] toTrimmedArray() {
		if(size == data.length)
			return data;
		
		int[] array = new int[size];
		for(int i = 0; i < size; i++)
			array[i] = data[i];
		
		return array;
		
	}
	
	public void trim() {
		data = toTrimmedArray();
	}
	
	public void sort() {
		Arrays.sort(data, 0, size);
	}
	
	public String toString() {
		return Arrays.toString(toTrimmedArray());
	}
	
	@Override
	public boolean equals(Object o) {
		IntegerList list = (IntegerList)o;
		return Arrays.equals(data, list.data);
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for(int i = 0; i < data.length; i++) {
			hash = hash*31 + data[i];
		}
		
		"".hashCode();
		
		return hash;
	}
	
}
