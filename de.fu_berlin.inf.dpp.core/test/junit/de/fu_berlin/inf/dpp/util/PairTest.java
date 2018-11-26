package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

/** @author Lindner, Andreas und Marcus */
public class PairTest {
  Function<Integer, Double> functionMock =
      new Function<Integer, Double>() {
        @Override
        public Double apply(Integer u) {
          return u.intValue() + 0.5;
        }
      };

  @Test
  public void testCross() {
    Object p = new Object();
    Collection<Object> vs = new ArrayList<Object>();
    vs.add(new Object());
    vs.add(new Object());
    vs.add(new Object());
    vs.add(new Object());

    Collection<Pair<Object, Object>> cross1 = Pair.cross(vs, p);
    Collection<Pair<Object, Object>> cross2 = Pair.cross(p, vs);

    // size of cross-collection has to be equal
    assertTrue(cross1.size() == vs.size());
    assertTrue(cross1.size() == cross2.size());

    for (Object v : vs) {
      // both cross-sets should contain all pairs
      assertTrue(cross1.contains(new Pair<Object, Object>(v, p)));
      assertTrue(cross2.contains(new Pair<Object, Object>(p, v)));
    }
  }

  @Test
  public void testDisjointPartition() {
    // in : {(1,6),(2,6),(1,7),(2,7),(1,8),(2,8)}
    // out: {(1,(6,7,8)), (2,(6,7,8))}

    List<Pair<Integer, Integer>> inList = new ArrayList<Pair<Integer, Integer>>(6);
    List<Pair<Integer, List<Integer>>> outList;

    HashSet<Integer> vSet = new HashSet<Integer>();
    vSet.add(6);
    vSet.add(7);
    vSet.add(8);
    vSet.add(Integer.MAX_VALUE);

    inList.add(new Pair<Integer, Integer>(1, 6));
    inList.add(new Pair<Integer, Integer>(2, 6));
    inList.add(new Pair<Integer, Integer>(1, 7));
    inList.add(new Pair<Integer, Integer>(2, 7));
    inList.add(new Pair<Integer, Integer>(1, 8));
    inList.add(new Pair<Integer, Integer>(2, 8));
    inList.add(new Pair<Integer, Integer>(1, Integer.MAX_VALUE));
    inList.add(new Pair<Integer, Integer>(2, Integer.MAX_VALUE));

    outList = Pair.disjointPartition(inList);

    assertTrue(outList.size() == 2);

    Iterator<Pair<Integer, List<Integer>>> iter = outList.iterator();

    while (iter.hasNext()) {
      Pair<Integer, List<Integer>> pair = iter.next();

      assertTrue((pair.p == 1) || (pair.p == 2));
      assertTrue(pair.v.size() == 4);

      Iterator<Integer> intIter = pair.v.iterator();
      Integer vOld = 0;
      Integer vNew;

      while (intIter.hasNext()) {
        vNew = intIter.next();

        assertTrue(vOld < vNew);
        assertTrue(vSet.contains(vNew));

        vOld = vNew;
      }
    }
  }

  @Test
  public void equals() {
    int p = 5;
    double v = 5.3;

    Pair<Integer, Double> pv1 = new Pair<Integer, Double>(p, v);
    Pair<Integer, Double> pv2 = new Pair<Integer, Double>(p, v);

    assertTrue(pv1 != pv2);
    assertTrue(pv1.equals(pv2));
  }

  @Test
  public void testExpand() {
    final Double[] doubles = new Double[] {5.0, 5.6, 6.5, 6.7, 8.4, 6.3};

    Function<Integer, Collection<Double>> f =
        new Function<Integer, Collection<Double>>() {
          @Override
          public Collection<Double> apply(Integer u) {
            Collection<Double> ret = new ArrayList<Double>();

            ret.add(doubles[(u.intValue() - 5) * 2]);
            ret.add(doubles[(u.intValue() - 5) * 2 + 1]);

            if (u.intValue() == 5) ret.add(doubles[5]);

            return ret;
          }
        };

    Collection<Integer> is = new ArrayList<Integer>();
    is.add(5);
    is.add(6);

    List<Double> expansion = Pair.expand(is, f);

    // first ensure that there are all return values of f in expansion
    for (Integer i : is) {
      Collection<Double> ds = f.apply(i);

      for (Double d : ds) assertTrue(expansion.contains(d));
    }

    // then ensure that there are no other return values of f in expansion
    for (Integer i : is) {
      Collection<Double> ds = f.apply(i);

      for (Double d : ds) expansion.remove(d);
    }
    assertTrue(expansion.size() == 0);
  }

  @Test
  public void testFlip() {
    int p = 5;
    double v = 5.3;

    Pair<Integer, Double> pv = new Pair<Integer, Double>(p, v);

    Pair<Double, Integer> vp = pv.flip();

    assertTrue(vp.v.equals(pv.p));
    assertTrue(vp.p.equals(pv.v));

    // fliplist is never used
  }

  @Test
  public void testHashcode() {
    int p = 5;
    double v1 = 5.3;
    double v2 = 5.5;

    Pair<Integer, Double> pv1 = new Pair<Integer, Double>(p, v1);
    Pair<Integer, Double> pv2 = new Pair<Integer, Double>(p, v1);
    Pair<Integer, Double> pv3 = new Pair<Integer, Double>(p, v2);

    assertTrue(pv1.hashCode() == pv2.hashCode());
    assertTrue(pv1.hashCode() != pv3.hashCode());
  }

  @Test
  public void testMap() {
    Collection<Integer> is = new ArrayList<Integer>();
    for (int i = 0; i < 10; i++) is.add((i * 3) + 25);

    List<Pair<Double, Integer>> mapping = Pair.map(is, functionMock);

    // check whether the mapping is equal in size and whether it contains
    // all maps
    assertTrue(mapping.size() == is.size());
    for (Integer i : is)
      assertTrue(mapping.contains(new Pair<Double, Integer>(functionMock.apply(i), i)));
  }

  @Test
  public void testPartition() {
    // in : vs --> {6,6,7,7,8,9} ; p(v) --> v + 0.5;
    // out: { (6.5,(6,6)) , (7.5,(7,7)) , (8.5,(8)) , (9.5,(9)) }

    Collection<Integer> vs = new ArrayList<Integer>(6);

    vs.add(6);
    vs.add(6);
    vs.add(7);
    vs.add(7);
    vs.add(8);
    vs.add(9);

    List<Pair<Double, List<Integer>>> partition = Pair.partition(vs, functionMock);

    assertTrue(partition.size() == 4);

    for (Pair<Double, List<Integer>> e : partition) {
      assertTrue(e.p != 0);
    }
  }

  @Test
  public void testPVCompare() {
    Pair<Integer, Double> pv34 = new Pair<Integer, Double>(3, 4.0);
    Pair<Integer, Double> pv58 = new Pair<Integer, Double>(5, 8.0);
    Pair<Integer, Double> pv56 = new Pair<Integer, Double>(5, 6.0);
    Pair<Integer, Double> pv76 = new Pair<Integer, Double>(7, 6.0);

    Comparator<Pair<Integer, Double>> cp = Pair.pCompare();
    assertTrue(cp.compare(pv34, pv58) < 0);
    assertTrue(cp.compare(pv58, pv56) == 0);
    assertTrue(cp.compare(pv76, pv56) > 0);

    Comparator<Pair<Integer, Double>> cv = Pair.vCompare();
    assertTrue(cv.compare(pv34, pv58) < 0);
    assertTrue(cv.compare(pv58, pv56) > 0);
    assertTrue(cv.compare(pv56, pv76) == 0);

    cp =
        Pair.pCompare(
            new Comparator<Integer>() {
              @Override
              public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
              }
            });
    assertTrue(cp.compare(pv34, pv58) > 0);
    assertTrue(cp.compare(pv58, pv56) == 0);
    assertTrue(cp.compare(pv76, pv56) < 0);

    cv =
        Pair.vCompare(
            new Comparator<Double>() {
              @Override
              public int compare(Double o1, Double o2) {
                return o2.compareTo(o1);
              }
            });
    assertTrue(cv.compare(pv34, pv58) > 0);
    assertTrue(cv.compare(pv58, pv56) < 0);
    assertTrue(cv.compare(pv56, pv76) == 0);
  }

  /*
   * @Test public void testPVList() { // never used }
   */

  /*
   * @Test public void testZip() { // never used }
   */
}
