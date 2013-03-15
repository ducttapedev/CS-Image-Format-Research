package util;


import java.util.Arrays;


public class General {

	
	

	
	public static boolean[] copy(boolean[] arr) {
		return Arrays.copyOf(arr, arr.length);
	}

	public static int[] copy(int[] arr) {
		return Arrays.copyOf(arr, arr.length);
	}
	public static int[][] copy(int[][] arr) {
		int[][] newArr = new int[arr.length][];
		for(int i = arr.length - 1; i >= 0; i--)
			newArr[i] = copy(arr[i]);
		
		return newArr;
	}
	
	public static int pow(int base, int exp) {
		if(exp == 1)
			return base;
		
		int value = pow(base, exp/2);
		if(exp%2 == 0)
			return value*value;
		else return value*value*base;
		
	}

	
}
