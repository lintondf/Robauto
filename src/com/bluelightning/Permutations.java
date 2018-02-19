package com.bluelightning;

import java.lang.reflect.Array;

// https://stackoverflow.com/questions/2920315/permutation-of-array

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class Permutations implements Iterator<Integer[]> {

	private Integer[] arr;
	private int[] ind;
	private boolean has_next;

	public Integer[] output;// next() returns this array, make it public
	
	Permutations(int n) {
		Integer[] arr = new Integer[n];
		for (int i = 0; i < n; i++) {
			arr[i] = i;
		}
		initialize(arr);
	}

	Permutations(Integer[] arr) {
		initialize(arr);
	}
	
	protected void initialize(Integer[] arr) {
		this.arr = arr.clone();
		ind = new int[arr.length];
		// convert an array of any elements into array of integers - first
		// occurrence is used to enumerate
		Map<Integer, Integer> hm = new HashMap<Integer, Integer>();
		for (int i = 0; i < arr.length; i++) {
			Integer n = hm.get(arr[i]);
			if (n == null) {
				hm.put(arr[i], i);
				n = i;
			}
			ind[i] = n.intValue();
		}
		Arrays.sort(ind);// start with ascending sequence of integers

		// output = new E[arr.length]; <-- cannot do in Java with generics, so
		// use reflection
		output = (Integer[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
		has_next = true;
	}

	public boolean hasNext() {
		return has_next;
	}

	/**
	 * Computes next permutations. Same array instance is returned every time!
	 * 
	 * @return
	 */
	public Integer[] next() {
		if (!has_next)
			throw new NoSuchElementException();

		for (int i = 0; i < ind.length; i++) {
			output[i] = arr[ind[i]];
		}

		// get next permutation
		has_next = false;
		for (int tail = ind.length - 1; tail > 0; tail--) {
			if (ind[tail - 1] < ind[tail]) {// still increasing

				// find last element which does not exceed ind[tail-1]
				int s = ind.length - 1;
				while (ind[tail - 1] >= ind[s])
					s--;

				swap(ind, tail - 1, s);

				// reverse order of elements in the tail
				for (int i = tail, j = ind.length - 1; i < j; i++, j--) {
					swap(ind, i, j);
				}
				has_next = true;
				break;
			}

		}
		return output;
	}

	private void swap(int[] arr, int i, int j) {
		int t = arr[i];
		arr[i] = arr[j];
		arr[j] = t;
	}

	public void remove() {

	}

	public static boolean equals(Integer[] a1, Integer[] a2) {
		if (a1.length != a2.length)
			return false;
		for (int i = 0; i < a1.length; i++) {
			if (a1[i] != a2[i])
				return false;
		}
		return true;
	}

	public static boolean contains(List<Integer[]> unique, Integer[] a) {
		for (Integer[] u : unique) {
			if (equals(u, a))
				return true;
		}
		return false;
	}

	public ArrayList<Integer[]> monotonic() {
		ArrayList<Integer[]> unique = new ArrayList<>();
		while (this.hasNext()) {
			Integer[] next = this.next().clone();
			for (int i = 1; i < next.length; i++) {
				if (next[i] < next[i - 1]) {
					List<Integer> list = Arrays.asList(next);
					next = list.subList(0, i).toArray(new Integer[i]);
					break;
				}
			}
			if (!Permutations.contains(unique, next)) {
				unique.add(next);
			}
		}
		return unique;
	}

}