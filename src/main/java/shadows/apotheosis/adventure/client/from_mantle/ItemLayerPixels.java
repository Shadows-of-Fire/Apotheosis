package shadows.apotheosis.adventure.client.from_mantle;

import java.util.BitSet;

/**
 * Keeps track of used pixels that have been "used" in an item layer model, to prevent z-fighting
 */
public class ItemLayerPixels {
  /** Each bitset represents whether pixels are used in the given row */
  private BitSet[] rows = new BitSet[0];
  private int width = 0;

  /** Calculates the GCD of two numbers using the Euclidean algorithm */
  private static int gcd(int a, int b) {
    while (b > 0) {
      int orig = b;
      b = a % b;
      a = orig;
    }
    return a;
  }

  /** Calculates the LCM of two numbers using the Euclidean algorithm */
  private static int lcm(int a, int b) {
    return a * (b / gcd(a, b));
  }

  /** Ensures the given size fits for setting */
  private void ensureSizeFits(int checkWidth, int checkHeight) {
    // if the size is currently 0, means we have just not instantiated anything yet, so use the value for the size
    int oldHeight = this.rows.length;
    if (oldHeight == 0 || this.width == 0) {
      this.rows = new BitSet[checkHeight];
      this.width = checkWidth;
    } else {
      // first, check that we have enough columns, may need to recreate bit sets if not
      // width is done first as it reduces the number of clones needed, as width has to clone every object, including those just created by height
      if (this.width % checkWidth != 0) {
        int newWidth = lcm(this.width, checkWidth);
        int xSpacing = newWidth / this.width;
        // stretch out old columns across the new columns
        for (int y = 0; y < oldHeight; y++) {
          BitSet oldRow = this.rows[y];
          if (oldRow != null) {
            BitSet newRow = this.rows[y] = new BitSet(newWidth);
            for (int i : oldRow.stream().toArray()) {
              int start = i * xSpacing;
              newRow.set(start, start + xSpacing);
            }
          }
        }
        this.width = newWidth;
      }
      // size is set, so we will have to resize. first check that we have enough rows, may need to create a new array if not
      if (oldHeight % checkHeight != 0) {
        int newHeight = lcm(oldHeight, checkHeight);
        int ySpacing = newHeight / oldHeight;
        BitSet[] newRows = new BitSet[newHeight];
        // stretch out all old rows across the new rows
        for (int y = 0; y < oldHeight; y++) {
          BitSet oldRow = this.rows[y];
          if (oldRow != null) {
            int start = y * ySpacing;
            newRows[start] = oldRow;
            for (int i = 1; i < ySpacing; i++) {
              newRows[start+i] = (BitSet) oldRow.clone();
            }
            newRows[y * ySpacing] = oldRow;
          }
          this.rows = newRows;
        }
      }
    }
  }

  /** Checks if the given pixel is set using internal coordinates */
  private boolean getInternal(int x, int y) {
    if (y <= rows.length) {
      BitSet set = rows[y];
      if (set != null) {
        return set.get(x);
      }
    }
    return false;
  }

  /**
   * Checks if the given pixel is set
   * @param x       X coordinate to get
   * @param y       Y coordinate to get
   * @param width   Width to scale the X coordinate
   * @param height  Height to scale the Y coordinate
   * @return  True if the bit is set, false if unset or out of bounds
   */
  public boolean get(int x, int y, int width, int height) {
    // if we have no data, obviously not here
    if (this.rows.length == 0 || this.width == 0) {
      return false;
    }
    // if we cannot cleanly divide by the width, this coordinate does not fit
    x *= this.width;
    if (x % width != 0) {
      return false;
    }
    y *= this.rows.length;
    if (y % height != 0) {
      return false;
    }
    // finish scaling coordinates
    return getInternal(x / width, y / height);
  }

  /**
   * Set the pixel for the given size
   * @param x       X coordinate to set
   * @param y       Y coordinate to set
   * @param width   Width to scale the X coordinate
   * @param height  Height to scale the Y coordinate
   */
  public void set(int x, int y, int width, int height) {
    if (x < 0 || x >= width) {
      throw new IllegalArgumentException("Parameter X must be between 0 and width");
    }
    if (y < 0 || y >= height) {
      throw new IllegalArgumentException("Parameter Y must be between 0 and height");
    }
    // resize if needed
    ensureSizeFits(width, height);
    // set all bits within a square represented by the "pixel size"
    // for instance, if using 32x textures and we are given a 16x, it will set 2x2 squares
    int xSize = this.width / width;
    int ySize = this.rows.length / height;
    x *= xSize;
    y *= ySize;
    for (int dy = 0; dy < ySize; dy++) {
      BitSet set = rows[y+dy];
      if (set == null) {
        set = rows[y+dy] = new BitSet(this.width);
      }
      set.set(x, x+xSize);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ItemLayerPixels:\n");
    for (int y = 0; y < this.rows.length; y++) {
      StringBuilder rowBuilder = new StringBuilder();
      for (int x = 0; x < this.width; x++) {
        rowBuilder.append(getInternal(x, y) ? 'X' : '_');
      }
      builder.append(rowBuilder.toString()).append('\n');
    }
    return builder.toString();
  }
}