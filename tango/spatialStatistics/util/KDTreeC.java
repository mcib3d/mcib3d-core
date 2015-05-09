
package tango.spatialStatistics.util;

import java.util.Arrays;
 
/**
 * This is a KD Bucket Tree, for fast sorting and searching of K dimensional data.
 * @author Chase
 *
 */
public class KDTreeC {
	/**
	 * Item, for moving data around.
	 * @author Chase
	 */
	public class Item {
		public double[] pnt;
		public Object obj;
		public double distance;
		private Item(double[] p, Object o) {
			pnt = p; obj = o;
		}
	}
	private final int dimensions;
	private final int bucket_size;
	private NodeKD root;
        private double[] scale;
	/**
	 * Constructor with value for dimensions.
	 * @param dimensions - Number of dimensions
	 */
	public KDTreeC(int dimensions) {
		this.dimensions = dimensions;
		this.bucket_size = 64;
		this.root = new NodeKD(this);
	}
 
	/**
	 * Constructor with value for dimensions and bucket size.
	 * @param dimensions - Number of dimensions
	 * @param bucket - Size of the buckets.
	 */
	public KDTreeC(int dimensions, int bucket) {
		this.dimensions = dimensions;
		this.bucket_size = bucket;
		this.root = new NodeKD(this);
	}
        
        public void setScaleSq(double[] scale_) {
            scale=scale_;
        }
 
	/**
	 * Add a key and its associated value to the tree.
	 * @param key - Key to add
	 * @param val - object to add
	 */
	public void add(double[] key, Object val) {
		root.add(new Item(key,val));
	}
 
	/**
	 * Returns all PointKD within a certain range defined by an upper and lower PointKD.
	 * @param low - lower bounds of area
	 * @param high - upper bounds of area
	 * @return - All PointKD between low and high.
	 */
	public Item[] getRange(double[] low, double[] high) {
		return root.range(high, low);
	}
 
	/**
	 * Gets the N nearest neighbors to the given key.
	 * @param key - Key
	 * @param num - Number of results
	 * @return Array of Item Objects, distances within the items
	 * are the square of the actual distance between them and the key
	 */
	public Item[] getNearestNeighbor(double[] key, int num) {
		ShiftArray arr = new ShiftArray(num);
		root.nearestn(arr, key);
		return arr.getArray();
	}
	/**
	 * Compares arrays of double and returns the euclidean distance
	 * between them.
	 * 
	 * @param a - The first set of numbers
	 * @param b - The second set of numbers
	 * @return The distance squared between <b>a</b> and <b>b</b>.
	 */
	public double distance(double[] a, double[] b) {
		double total = 0;
		for (int i = 0; i < a.length; ++i)
			total += (b[i] - a[i]) * (b[i] - a[i]) * scale[i];
		return Math.sqrt(total);
	}
	/**
	 * Compares arrays of double and returns the squared euclidean distance
	 * between them.
	 * 
	 * @param a - The first set of numbers
	 * @param b - The second set of numbers
	 * @return The distance squared between <b>a</b> and <b>b</b>.
	 */
	public double distanceSq(double[] a, double[] b) {
		double total = 0;
		for (int i = 0; i < a.length; ++i)
			total += (b[i] - a[i]) * (b[i] - a[i]) * scale[i];
		return total;
	}
 
	//Internal tree node
	private class NodeKD {
		private KDTreeC owner;
		private NodeKD left, right;
		private double[] upper, lower;
		private Item[] bucket;
		private int current, dim;
		private double slice;
 
		//note: we always start as a bucket
		private NodeKD(KDTreeC own) {
			owner = own;
			upper = lower = null;
			left = right = null;
			bucket = new Item[own.bucket_size];
			current = 0;
			dim = 0;
		}
		//when we create non-root nodes within this class
		//we use this one here
		private NodeKD(NodeKD node) {
			owner = node.owner;
			dim = node.dim + 1;
			bucket = new Item[owner.bucket_size];
			if(dim + 1 > owner.dimensions) dim = 0;
			left = right = null;
			upper = lower = null;
			current = 0;
		}
		//what it says on the tin
		private void add(Item m) {
			if(bucket == null) {
				//Branch
				if(m.pnt[dim] > slice)
					right.add(m);
				else left.add(m);
			} else {
				//Bucket
				if(current+1>owner.bucket_size) {
					split(m);
					return;
				}
				bucket[current++] = m;
			}
			expand(m.pnt);
		}
		//nearest neighbor thing
		private void nearestn(ShiftArray arr, double[] data) {
			if(bucket == null) {
				//Branch
				if(data[dim] > slice) {
					right.nearestn(arr, data);
					if(left.current != 0) {
						if(owner.distanceSq(left.nearestRect(data),data)
								< arr.getLongest()) {
							left.nearestn(arr, data);
						}
					}
 
				} else {
					left.nearestn(arr, data);
					if(right.current != 0) {
						if(owner.distanceSq(right.nearestRect(data),data) 
								< arr.getLongest()) {
							right.nearestn(arr, data);
						}
					}
				}
			} else {
				//Bucket
				for(int i = 0; i < current; i++) {
					bucket[i].distance = owner.distanceSq(bucket[i].pnt, data);
					arr.add(bucket[i]);
				}
			}
		}
		//gets all items from within a range
		private Item[] range(double[] upper, double[] lower) {
			//TODO: clean this up a bit
			if(bucket == null) {
				//Branch
				Item[] tmp = new Item[0];
				if (intersects(upper,lower,left.upper,left.lower)) {
					Item[] tmpl = left.range(upper,lower);
					if(0 == tmp.length)
						tmp = tmpl;
				}
				if (intersects(upper,lower,right.upper,right.lower)) {
					Item[] tmpr = right.range(upper,lower);
					if (0 == tmp.length)
						tmp = tmpr;
					else if (0 < tmpr.length) {
						Item[] tmp2 = new Item[tmp.length + tmpr.length];
						System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
						System.arraycopy(tmpr, 0, tmp2, tmp.length, tmpr.length);
						tmp = tmp2;
					}
				}
				return tmp;
			}
			//Bucket
			Item[] tmp = new Item[current];
			int n = 0;
			for (int i = 0; i < current; i++) {
				if (contains(upper, lower, bucket[i].pnt)) {
					tmp[n++] = bucket[i];
				}
			}
			Item[] tmp2 = new Item[n];
			System.arraycopy(tmp, 0, tmp2, 0, n);
			return tmp2;
		}
 
		//These are helper functions from here down
		//check if this hyper rectangle contains a give hyper-point
		public boolean contains(double[] upper, double[] lower, double[] point) {
			if(current == 0) return false;
			for(int i=0; i<point.length; ++i) {
				if(point[i] > upper[i] ||
					point[i] < lower[i]) 
						return false;
			}
			return true;
		}
		//checks if two hyper-rectangles intersect
		public boolean intersects(double[] up0, double[] low0,
				double[] up1, double[] low1) {
			for(int i=0; i<up0.length; ++i) {
				if(up1[i] < low0[i] || low1[i] > up0[i]) return false;
			}
			return true;
		}
		//splits a bucket into a branch
		private void split(Item m) {
			//split based on our bound data
			slice = (upper[dim]+lower[dim])/2.0;
			left = new NodeKD(this);
			right = new NodeKD(this);
			for(int i=0; i<current; ++i) {
				if(bucket[i].pnt[dim] > slice)
					right.add(bucket[i]);
				else left.add(bucket[i]);
			}
			bucket = null;
			add(m);
		}
		//gets nearest point to data within this hyper rectangle
		private double[] nearestRect(double[] data) {
			double[] nearest = data.clone();
			for(int i = 0; i < data.length; ++i) {
				if(nearest[i] > upper[i]) nearest[i] = upper[i];
				if(nearest[i] < lower[i]) nearest[i] = lower[i];
			}
			return nearest;
		}
		//expands this hyper rectangle
		private void expand(double[] data) {
			if(upper == null) {
				upper = Arrays.copyOf(data, owner.dimensions);
				lower = Arrays.copyOf(data, owner.dimensions);
				return;
			}
			for(int i=0; i<data.length; ++i) {
				if(upper[i] < data[i]) upper[i] = data[i];
				if(lower[i] > data[i]) lower[i] = data[i];
			}
		}
	}
	//A simple shift array that sifts data up
	//as we add new ones to lower in the array.
	private class ShiftArray {
		private Item[] items;
		private final int max;
		private int current;
		private ShiftArray(int maximum) {
			max = maximum;
			current = 0;
			items = new Item[max];
		}
		private void add(Item m) {
			int i;
			for(i=current;i>0&&items[i-1].distance >  m.distance; --i);
			if(i >= max) return;
			if(current < max) ++current;
			System.arraycopy(items, i, items, i+1, current-(i+1));
			items[i] = m;
		}
		private double getLongest() {
			if (current < max) return Double.POSITIVE_INFINITY;
			return items[max-1].distance;
		}
		private Item[] getArray() {
			return items.clone();
		}
	}
}
