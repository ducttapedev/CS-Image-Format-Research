package util;


public class IntegerStack extends IntegerList {
	
	/**
	 * Removes and returns the element at the top of the stack
	 */
	public int pop() {
		return remove(size - 1);
	}
	
	/**
	 * Returns the element at the top of the stack
	 */
	public int peek() {
		return get(size - 1);
	}
	
	/**
	 * Push <code>element</code> onto the top of the stack
	 */
	public void push(int element) {
		add(element);
	}
	
	public static void main(String[] args) {
		
	}
	
}
