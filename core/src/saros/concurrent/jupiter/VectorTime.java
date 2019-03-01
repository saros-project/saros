package saros.concurrent.jupiter;

/**
 * Vector time is a concept used in almost all Operational Transformation algorithms to determine
 * causality relations of operations.
 */
public interface VectorTime extends Timestamp {

  /**
   * Gets the length of the vector.
   *
   * @return the length of the vector time
   */
  int getLength();

  /**
   * Gets the value at the given index.
   *
   * @param index the index into the vector
   * @return the value at the given index
   * @throws IndexOutOfBoundsException if index is invalid
   */
  int getAt(int index);
}
