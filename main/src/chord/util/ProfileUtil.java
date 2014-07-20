package chord.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chord.util.CollectionUtil.compare;
import static chord.util.ExceptionUtil.fail;

/** Profiling utilities.
  This class is meant to help identify algorithmic bottlenecks. It is *not*
  meant to help with micro-benchmarks. The typical use is
    void possiblyRecursiveFunction() {
      ProfileUtil.start("MAIN", "possiblyRecFun");
      // or ProfileUtil.start("MAIN");
      ...
      ProfileUtil.stop("MAIN");
      // or ProfileUtil.stop("MAIN", "possiblyRecFun"); // checks nesting
    }
    ...
    void main(...) {
      ...
      ProfileUtil.printStats(System.out, 3);
    }
  The first argument of {@code start}/{@code stop} is a category name.
  Different categories are tracked independently, in parallel. For each
  category, the result is a forest of times, whose nodes could be labelled if
  the second argument to {@code start} is provided.

  NOTE: If you want to turn off profiling (and most of its overhead) then see
  {@code inactive} below.

  NOTE: This class is *not* thread safe.
*/
public class ProfileUtil {
  // Set to |true| to get rid of (most of) profiling overhead.
  private static final boolean inactive = false;

  public static void start(String category, String label) {
    if (inactive) return;
    ArrayList<Time> ts = ongoing.get(category);
    if (ts == null) {
      ts = Lists.newArrayList();
      ongoing.put(category, ts);
    }
    Time t = new Time(label);
    ts.add(t);
    if (ts.size() > 1) {
      Time s = ts.get(ts.size() - 2);
      s.children.add(t);
    }
  }

  public static void stop(String category, String label) {
    if (inactive) return;
    ArrayList<Time> ts = ongoing.get(category);
    if (ts == null || ts.isEmpty())
      fail("profile category stoped before started: " + category);
    Time t = ts.remove(ts.size() - 1);
    if (label != null && !label.equals(t.label))
      fail("started " + t.label + " and stopped " + label);
    t.time = System.nanoTime() - t.time;
    if (ts.isEmpty()) {
      ArrayList<Time> ds = done.get(category);
      if (ds == null) {
        ds = Lists.newArrayList();
        done.put(category, ds);
      }
      ds.add(t);
    }
  }

  public static void printStats(PrintWriter out, int depth) {
    if (inactive) return;
    out.printf("PROFILE START {%n");
    out.printf("PROFILE FORMAT total={+(avg±dev)*cnt}%n");
    for (Map.Entry<String,ArrayList<Time>> e : done.entrySet()) {
      out.printf("PROFILE CATEGORY %s%n", e.getKey());
      Time t = new Time("ROOT");
      t.children = e.getValue();
      t.time = 0;
      for (Time u : t.children) t.time += u.time;
      t.print(out, 0, depth);
    }
    long now = System.nanoTime();
    for (Map.Entry<String,ArrayList<Time>> e : ongoing.entrySet()) {
      ArrayList<Time> ts = e.getValue();
      if (ts.isEmpty()) continue;
      double delta = 1e-9 * (now - ts.get(0).time);
      out.printf("PROFILE LEFTOVER %s %.02f%n", e.getKey(), delta);
    }
    out.printf("PROFILE STOP }%n");
  }

  public static void printStats(PrintStream out, int depth) {
    if (inactive) return;
    PrintWriter w = new PrintWriter(out);
    printStats(w, depth);
    w.flush();
    w.close();
  }

  public static void start(String category) {
    start(category, "anonymous");
  }

  public static void stop(String category) {
    stop(category, null);
  }

  private static class Time {
    long time; // start time for ongoing nodes, total time for done nodes
    String label;
    List<Time> children;

    Time(final String label) {
      this.time = System.nanoTime();
      this.label = label;
      this.children = new ArrayList<Time>();
    }

    void print(PrintWriter out, int depth, int depthLimit) {
      int i;
      if (depth == depthLimit) return;

      // Print self line.
      out.printf("PROFILE ");
      for (i = 0; i <= depth; ++i) out.printf("  ");
      out.printf("%s %.02f=", label, time * (1e-9));

      Map<String, List<Double>> ts = Maps.newHashMap();
      for (Time t : children) {
        List<Double> ds = ts.get(t.label);
        if (ds == null) ds = Lists.newArrayList();
        ds.add(t.time * (1e-9));
        ts.put(t.label, ds);
      }
      List<AvgDevCntLbl> xs = Lists.newArrayList();
      for (Map.Entry<String, List<Double>> t : ts.entrySet()) {
        List<Double> ds = t.getValue();
        double avg = 0.0;
        for (double d : ds) avg += d;
        avg /= ds.size();
        double dev = 0.0;
        for (double d : ds) dev += (d - avg) * (d - avg);
        dev = Math.sqrt(dev / t.getValue().size() - 1);
        xs.add(new AvgDevCntLbl(avg, dev, ds.size(), t.getKey()));
      }
      long self = time;
      for (Time t : children) self -= t.time;
      xs.add(new AvgDevCntLbl(self * (1e-9), 0.0, 1, "SELF"));
      Collections.sort(xs);
      for (AvgDevCntLbl x : xs) {
        if (Double.isNaN(x.dev) || x.dev < 1.0)
          out.printf("+%.02f*(%d %s)", x.avg, x.cnt, x.lbl);
        else
          out.printf("+(%.02f±%.02f)*(%d %s)", x.avg, x.dev, x.cnt, x.lbl);
      }
      out.println();

      // Recurse, but only in the most time consuming children.
      List<Time> cs = Lists.newArrayList(children);
      Collections.sort(cs, new Comparator<Time>() {
        @Override public int compare(Time t1, Time t2) {
          return CollectionUtil.compare(t2.time, t1.time);
        }
      });
      for (i = 0; i < Math.min(cs.size(), 10); ++i)
        cs.get(i).print(out, depth + 1, depthLimit);
      if (i < cs.size()) {
        out.printf("PROFILE ");
        for (int j = 0; j <= depth; ++j) out.printf("  ");
        out.printf("... and %d more%n", cs.size() - i);
      }
    }
  }

  static final class AvgDevCntLbl implements Comparable<AvgDevCntLbl> {
    final public double avg;
    final public double dev;
    final public int cnt;
    final public String lbl;

    final public double tot; // total time, computed from the fields above

    AvgDevCntLbl(
        final double avg,
        final double dev,
        final int cnt,
        final String lbl
    ) {
      Preconditions.checkNotNull(lbl);
      this.avg = avg;
      this.dev = dev;
      this.cnt = cnt;
      this.lbl = lbl;
      this.tot = avg * cnt;
    }

    @Override public int compareTo(AvgDevCntLbl o) {
      if (!eqd(tot, o.tot)) return Double.compare(o.tot, tot);
      if (!eqd(avg, o.avg)) return Double.compare(o.avg, avg);
      if (!eqd(dev, o.dev)) return Double.compare(o.dev, dev);
      if (cnt != o.cnt) return CollectionUtil.compare(o.cnt, cnt);
      return lbl.compareTo(o.lbl);
    }

    private boolean eqd(double a, double b) {
      return
        Math.abs(a-b) < EPSILON
        || (Math.abs(b) > EPSILON && Math.abs(a/b-1.0) < EPSILON)
        || (Math.abs(a) > EPSILON && Math.abs(b/a-1.0) < EPSILON);
    }

    private final static double EPSILON = 1e-9;
  }


  //  For each category |c|:
  //    - the list |done.get(c)| contains the roots of the trees in a forest
  //    - the stack |ongoing.get(c)| goes from the root of the tree being built
  //      to a leaf; an "ongoing node" is one that appears in |ongoing.get(c)|,
  //      *not* including their children
  //  NOTE: http://rgrig.blogspot.com/2009/08/java-is-fast-they-say-on-java.html
  //  explains why I use ArrayList to represent stacks.
  private static final HashMap<String, ArrayList<Time>> ongoing = Maps.newHashMap();
  private static final HashMap<String, ArrayList<Time>> done = Maps.newHashMap();

  private ProfileUtil() { /* no instances */ }
}
