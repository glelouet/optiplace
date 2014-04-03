/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.plan.choco;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import entropy.configuration.*;
import entropy.plan.*;
import entropy.plan.durationEvaluator.DurationEvaluator;
import entropy.plan.partitioner.*;
import entropy.vjob.*;

/** @author Fabien Hermenier */
public class CustomizableSplitablePlannerModule extends
    CustomizablePlannerModule {

  private boolean repair = true;

  private ArrayList<PlanThread> subs;

  // private ArrayList<SolvingStatistics> solvingStatistics;
  // private ArrayList<List<SolutionStatistics>> solutionStatistics;

  public static enum PartitioningMode {
    none, sequential, parallel
  }

  private PartitioningMode partMode = PartitioningMode.none;

  public CustomizableSplitablePlannerModule(DurationEvaluator d) {
    super(d);
    subs = new ArrayList<PlanThread>();
    // solutionStatistics = new ArrayList<List<SolutionStatistics>>();
    // solvingStatistics = new ArrayList<SolvingStatistics>();
  }

  @Override
  public entropy.plan.ReconfigurationResult compute(Configuration src,
      ManagedElementSet<VirtualMachine> run,
      ManagedElementSet<VirtualMachine> wait,
      ManagedElementSet<VirtualMachine> sleep,
      ManagedElementSet<VirtualMachine> stop, ManagedElementSet<Node> on,
      ManagedElementSet<Node> off, List<VJob> queue) throws PlanException {

    subs.clear();
    // solutionStatistics.clear();
    // solvingStatistics.clear();

    List<Partition> parts;

    int nbConstraints = 0;
    for (VJob v : queue) {
      nbConstraints += v.getConstraints().size();
    }
    if (nbConstraints == 0 || partMode == PartitioningMode.none) {
      if (nbConstraints == 0) {
        logger.debug("No partitioning as there is no placement constraints");
      } else {
        logger.debug("No partitioning allowed");
      }
      parts = new LinkedList<Partition>();
      Partition p = new Partition(0);
      p.getNodes().addAll(src.getAllNodes());
      p.getVirtualMachines().addAll(src.getAllVirtualMachines());
      for (VJob v : queue) {
        if (v.getConstraints() != null) {
          p.getConstraints().addAll(v.getConstraints());
        }
      }
      parts.add(p);
    } else {
      PlanPartitioner partitioner = new OtherPartitioning(src);
      // We have to push all the constraints, expect fences at the end
      List<PlacementConstraint> cs = new LinkedList<PlacementConstraint>();
      for (VJob v : queue) {
        for (PlacementConstraint c : v.getConstraints()) {
          if (!(c instanceof Fence)) {
            cs.add(c);
          } else {
            // if (c instanceof Fence) {
            try {
              // Plan.logger.info("Part wrt. " + c + " " +
              // c.getNodes());
              partitioner.part((Fence) c);
            }
            catch (PartitioningException e) {
              Planner.logger.error(e.getMessage(), e);
            }
          }
        }
      }
      for (PlacementConstraint c : cs) {
        try {
          if (c instanceof Spread) {
            partitioner.part((Spread) c);
          } else if (c instanceof Ban) {
            partitioner.part((Ban) c);
          } /* else if (c instanceof OneOf) { partitioner.part((OneOf) c); }
             * else { Plan.logger.warn("Unsupported constraint: " + c); } */
        }
        catch (PartitioningException e) {
          Planner.logger.error(e.getMessage(), e);
        }
      }
      parts = partitioner.getResultingPartitions();
      logger.debug("Problem was splitted into " + parts.size()
          + ". Solving method: " + partMode);
    }
    generationTime = -1;
    for (Partition p : parts) {
      try {
        PlanThread t = new PlanThread(p, getDurationEvaluator(), src, run,
            wait, sleep, stop, on, off);
        t.setTimeout(getTimeLimit());
        t.setRepairMode(repair);
        subs.add(t);
        t.start();
        if (partMode == PartitioningMode.none
            || partMode == PartitioningMode.sequential) {
          try {
            t.join();
            System.gc(); // Clear memory please
          }
          catch (InterruptedException e) {
            Planner.logger.error(e.getMessage(), e);
          }
        }
        long g = t.getGenerationTime();
        if (g > generationTime) {
          generationTime = g;
        }
      }
      catch (ConfigurationsException e) {
        Planner.logger.error(e.getMessage(), e);
      }
    }

    if (partMode == PartitioningMode.parallel) {
      for (PlanThread t : subs) {
        try {
          t.join();
        }
        catch (InterruptedException e) {
          Planner.logger.error(e.getMessage(), e);
        }
      }
    }

    StringBuilder fullEx = new StringBuilder();
    List<entropy.plan.ReconfigurationResult> ress = new LinkedList<entropy.plan.ReconfigurationResult>();
    for (PlanThread t : subs) {
      if (t.getException() == null) {
        entropy.plan.ReconfigurationResult r = t.getResultingPlan();
        if (r != null) {
          // logger.debug(r.toString());
          ress.add(r);
        }
      } else {
        fullEx.append(t.getRunID()).append(": ").append(t.getException());
      }
    }
    if (fullEx.length() > 0) {
      throw new PlanException(fullEx.toString());
    }
    try {
      return entropy.plan.ReconfigurationResults.merge(ress);
    }
    catch (entropy.plan.ReconfigurationResultsException e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  /** Get the solving statistics. If there is only one sub problem, all the
   * statistics are returned. Otherwise, the last statistics of each sub problem
   * are merged: Opened nodes, backtracks and objective values are summed while
   * the maximum timecount is returned.
   * @return a list of statistics that may be empty */
  @Override
  public List<SolutionStatistics> getSolutionsStatistics() {
    // We retrieve the last statistics of each model, sum counters
    // and the maximum timecount
    if (subs.size() == 1) {
      return subs.get(0).getSolutionsStatistics();
    } else {
      int nbNodes = 0;
      int nbBacktracks = 0;
      int objectives = 0;
      int timecount = 0;
      for (PlanThread t : subs) {
        SolutionStatistics st = t.getSolutionsStatistics().get(
            t.getSolutionsStatistics().size() - 1);
        nbNodes += st.getNbNodes();
        nbBacktracks += st.getNbBacktracks();
        objectives += st.getObjective();
        if (st.getTimeCount() > timecount) {
          timecount = st.getTimeCount();
        }
      }
      List<SolutionStatistics> res = new ArrayList<SolutionStatistics>();
      res.add(new SolutionStatistics(nbNodes, nbBacktracks, timecount,
          objectives));
      return res;
    }
  }

  @Override
  public SolvingStatistics getSolvingStatistics() {
    int nbNodes = 0;
    int nbBacktracks = 0;
    boolean timeout = false;
    int timecount = 0;

    for (PlanThread t : subs) {
      SolvingStatistics s = t.getSolvingStatistics();
      if (s == null) {
        return SolvingStatistics.getStatisticsForNotSolvingProcess();
      }
      nbNodes += s.getNbNodes();
      nbBacktracks += s.getNbBacktracks();
      timeout |= s.hasReachedTimeout();
      if (s.getTimeCount() > timecount) {
        timecount = s.getTimeCount();
      }
    }
    return new SolvingStatistics(nbNodes, nbBacktracks, timecount, timeout);
  }

  public void setPartitioningMode(PartitioningMode m) {
    partMode = m;
  }

  public PartitioningMode getPartitioningMode() {
    return partMode;
  }

  /** Use the repair mode.
   * @param b {@code true} to use the repair mode */
  public void setRepairMode(boolean b) {
    repair = b;
  }

  public boolean isRepairModeUsed() {
    return repair;
  }

}
