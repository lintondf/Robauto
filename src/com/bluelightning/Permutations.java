package com.bluelightning;



// https://stackoverflow.com/questions/2920315/permutation-of-array

import java.util.ArrayList;

public class Permutations {

	private Integer number;
	
	public Permutations(int number) {
		this.number = number;
		//RobautoMain.logger.trace("Permutations " + number);
	}

	public ArrayList<Integer[]> monotonic() {
		//RobautoMain.logger.trace("monotonic");
		int N = 1 << number;
		//RobautoMain.logger.trace("monotonic " + N);
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