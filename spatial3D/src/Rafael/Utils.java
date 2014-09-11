/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Rafael;

/**
 *
 * @author ttnhoa
 */

import java.util.ArrayList;
import java.util.Random;

public class Utils {
  public static void shuffleList(ArrayList<Integer> a) {
    int n = a.size();
    Random random = new Random();
    random.nextInt();
    for (int i = 0; i < n; i++) {
      int change = i + random.nextInt(n - i);
      swap(a, i, change);
    }
  }
  public static void shuffleFisherYates(ArrayList<Integer> a) 
  {
      int n = a.size(), t, i;
      while(n > 0)
      {
          i = (int)Math.floor(Math.random() * n--);
          swap(a, n, i);
      }    
  }
  
  public static void shuffleNaiveSwapI2Random(ArrayList<Integer> a)
  {
      int n = a.size(), i = -1, j;
      while(++i < n)
      {
          j = (int)Math.floor(Math.random() * n);
          swap(a, j, i);
      }    
  } 
  public static void shuffleNaiveSwapRandom2Random(ArrayList<Integer> a)
  {
      int n = a.size(), i = -1, j, k;
      
      while(++i < n)
      {
          j = (int)Math.floor(Math.random() * n);
          k = (int)Math.floor(Math.random() * n);
          swap(a, j, k);
      }    
  }
  
  private static void swap(ArrayList<Integer> a, int i, int change) {
    int helper = a.get(i);
    a.set(i, a.get(change));
    a.set(change, helper);
  }

 /* public static void main(String[] args) {
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    list.add(3);
    list.add(4);
    list.add(5);
    list.add(6);
    list.add(7);
    //shuffleList(list);
    //shuffleFisherYates(list);
    shuffleNaiveSwapRandom2Random(list);
    for (int i : list) {
      System.out.println(i);
    }
  }*/
} 
