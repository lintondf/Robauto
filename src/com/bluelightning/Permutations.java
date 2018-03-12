package com.bluelightning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;

// https://stackoverflow.com/questions/2920315/permutation-of-array

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.bluelightning.data.TripPlan;

public class Permutations {

	private Integer number;
	
	public Permutations(int number) {
		this.number = number;
		Main.logger.trace("Permutations " + number);
	}

	public ArrayList<Integer[]> monotonic() {
		Main.logger.trace("monotonic");
		int N = 1 << number;
		Main.logger.trace("monotonic " + N);
		ArrayList<Integer[]> results = new ArrayList<>();
		if (number <= 1) {
			results.add( new Integer[]{0} );
			return results;
		}
		for (int i = 1; i < N; i++) {
			int w = i;
			ArrayList<Integer> members = new ArrayList<>();
			int pos = 0;
			while (w != 0) {
				if ( (w & 1) == 1) {
					members.add(pos);
				}
				w = w >> 1;
				pos++;
			}
			results.add(members.toArray(new Integer[members.size()]));
		}
		return results;
	}
	
	
	public static void main(String[] args ) {
		for (int i = 1; i < 12; i++) {
			Permutations perm = new Permutations(i);
			System.out.println(perm.monotonic().size());
		}
	}

}