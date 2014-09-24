/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */
package entropy.controlLoop;

import java.util.*;

import entropy.configuration.*;
import entropy.decision.AssignmentException;
import entropy.decision.predictor.TendencyBasedDecisionModule;
import entropy.execution.TimedReconfigurationExecuter;
import entropy.monitoring.Monitor;
import entropy.monitoring.MonitoringException;
import entropy.plan.CustomizablePlanner;
import entropy.plan.PlanException;
import entropy.plan.entropy.plan.ReconfigurationResult;
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.durationEvaluator.DurationEvaluator;
import entropy.plan.parser.PlainTextTimedReconfigurationPlanSerializer;
import entropy.vjob.VJob;
import entropy.vjob.builder.VJobBuilderFactory;
import entropy.vjob.queue.VJobsPool;

/** A basic control loop where the decision module and the planner module is
 * customizable with several constraints.
 * @author Fabien Hermenier */
public class CustomizableControlLoop extends ControlLoop implements Runnable {

  /** The queue of VJobs. */
  private VJobsPool queue;

  /** The builder to make the VJobs. */
  private VJobBuilderFactory builder;

  /** The executer to perform the actions. */
  private TimedReconfigurationExecuter exec;

  /** The module to monitor the infrastructure. */
  private Monitor monitoring;

  /** Perform auto-reconfiguration or not. */
  private boolean autoReconf = true;
  /** The decision module to estimate the demand of the VMs and their state
   * (don't touch to the state here). */
  private TendencyBasedDecisionModule decision;

  /** Lock for state analyser. */
  private final Object refreshLock = new Object();

  /** The configuration expected by the decision module. */
  private Configuration currentExpected;

  /** The hosting pool. */
  private List<VJob> currentVJob;

  /** Delay in seconds between two iterations of the loop. */
  private int refreshDelay = 10;

  /** Stop the loop ? */
  private boolean stop = false;

  /** Indicates the loop is performing a reconfiguration. */
  private boolean isReconfiguring;

  /** The plan module to assign the VMs and plan the actions. */
  private CustomizablePlanner planner;

  /** Make a new loop.
   * @param monitor The monitor to use
   * @param pool the pool of vjobs
   * @param builder the VJobBuilder to use
   * @param eval The durationEvaluator to use
   * @param e the execution module to use */
  public CustomizableControlLoop(Monitor monitor, VJobsPool pool,
      VJobBuilderFactory builder, DurationEvaluator eval,
      TimedReconfigurationExecuter e) {
    this.builder = builder;
    monitoring = monitor;
    exec = e;
    queue = pool;

    decision = new TendencyBasedDecisionModule();
    planner = new ChocoCustomRP(eval);
    currentVJob = new LinkedList<VJob>();
    makeCurrents();
    new Thread(this).start();
  }

  /** Run the loop once.
   * @return true to stop the loop. */
  @Override
  public boolean runLoop() {

    Configuration expected = null;
    Date timeStamp = Calendar.getInstance().getTime();
    entropy.plan.ReconfigurationResult plan = null;
    try {
      // cur = monitoring.getConfiguration();

      List<VJob> vjobs;
      synchronized (refreshLock) {
        expected = currentExpected.clone();
        vjobs = new ArrayList<VJob>(currentVJob);
      }

      builder.useConfiguration(expected);
      getLogger().debug(
          "Offlines: " + expected.getOfflines().size() + ", onlines: "
              + expected.getOnlines().size() + ", overloaded nodes: "
              + ConfigurationUtils.futureOverloadedNodes(expected).size());
      getLogger().debug(
          "Runnings: " + expected.getRunnings().size() + ", waitings: "
              + expected.getWaitings().size() + ", sleeping: "
              + expected.getSleepings().size());
      StringBuilder b = new StringBuilder();
      for (Iterator<VJob> ite = vjobs.iterator(); ite.hasNext();) {
        VJob v = ite.next();
        b.append(v.id());
        if (ite.hasNext()) {
          b.append(", ");
        }
      }
      getLogger().debug("VJobs: " + b.toString());
      ManagedElementSet<VirtualMachine> allRunnings = new SimpleManagedElementSet<VirtualMachine>();
      allRunnings.addAll(expected.getRunnings());
      allRunnings.addAll(expected.getWaitings());
      if (!autoReconf) {
        getLogger().debug("No reconfiguration allowed.");
        return false;
      }
      plan = planner.compute(expected,
          allRunnings,
          new SimpleManagedElementSet<VirtualMachine>(),
          // expected.getWaitings(),
          expected.getSleepings(),
          new SimpleManagedElementSet<VirtualMachine>(), expected.getOnlines(),
          expected.getOfflines(), vjobs);
      if (plan.size() > 0) {
        getLogger().debug(plan.size() + " actions to execute:\n" + plan);
        isReconfiguring = true;
        exec.start(plan);
        isReconfiguring = false;
      } else {
        getLogger().info("No reconfiguration is necessary");
      }
    }
    catch (PlanException e) {
      getLogger().error(e.getMessage(), e);
    }
    finally {
      if (expected != null) {
        if (getLogger().isDebugEnabled()) {
          String file = logConfiguration(expected, timeStamp, "src");
          getLogger()
              .info("Source configuration available into '" + file + "'");
        }
        if (plan != null && plan.size() > 0) {
          String file = logPlan(plan, timeStamp, "plan");
          getLogger().info("Plan available into '" + file + "'");
        }
      }
    }
    return false;
  }

  /** Set the timeout of the decision module.
   * @param seconds the maximum duration of the solving process in seconds. */
  public void setAssignTimeout(int seconds) {
    decision.setTimeout(seconds);
  }

  /** Get the timeout value of the decision module.
   * @return a duration, in seconds */
  public int getAssignTimeout() {
    return decision.getTimeout();
  }

  /** Set the timeout of the plan module.
   * @param seconds the maximum duration of the solving process in seconds. */
  public void setPlanTimeout(int seconds) {
    planner.setTimeLimit(seconds);
  }

  /** Get the timeout value of the plan module.
   * @return a duration, in seconds */
  public int getPlanTimeout() {
    return planner.getTimeLimit();
  }

  public void setPredictionStep(int st) {
    decision.setStep(st);
  }

  public int getPredictionStep() {
    return decision.getStep();
  }

  @Override
  public void destroy() {
    stop = true;
  }

  private void makeCurrents() {
    try {
      synchronized (refreshLock) {
        // Get the configuration
        Configuration cur = monitoring.getConfiguration();
        currentExpected = decision.compute(cur);
        lightConfiguration(currentExpected);

        List<VJob> vjobs = queue.getRunningPriorities();

        // Decorate the current configuration. Unknown VMs are put
        // into the waiting state
        for (VJob v : vjobs) {
          for (VirtualMachine vm : v.getVirtualMachines()) {
            if (currentExpected.getAllVirtualMachines().get(vm.getName()) == null) {
              currentExpected.addWaiting(vm);
            }
          }
        }

        currentVJob.clear();
        currentVJob.addAll(vjobs);
      }
    }
    catch (AssignmentException e) {
      getLogger().error(e.getMessage(), e);
    }
    catch (MonitoringException e) {
      getLogger().error(e.getMessage(), e);
    }
  }

  public String analyzeFile = "target/analyze_" + new Date() + ".txt";

  @Override
  public void run() {
    StateAnalyzer analyzer = new StateAnalyzer(analyzeFile);
    while (!stop) {
      try {
        Thread.sleep(refreshDelay * 1000L);
        makeCurrents();
        synchronized (refreshLock) {
          analyzer.analyze(currentExpected, currentVJob, isReconfiguring);
          getLogger().debug("Refreshing expected configuration & vjobs");
        }
        // Wait
      }
      catch (InterruptedException e) {
        getLogger().warn(e.getMessage(), e);
      }
    }
  }

  /** Log a plan into a file. If an error occurs, it is logged at the error level
   * @param p the plan to store
   * @param timeStamp the timeStamp for the configuration
   * @param suffix the suffix of the log file
   * @return the pathname of the log file */
  public String logPlan(entropy.plan.ReconfigurationResult p, Date timeStamp,
      String suffix) {
    if (getLogsDir() != null) {
      String filename = getLogsDir() + "/" + DATE_FORMAT.format(timeStamp)
          + "/" + HOUR_FORMAT.format(timeStamp) + "-" + suffix + ".txt";
      try {
        PlainTextTimedReconfigurationPlanSerializer.getInstance().write(p,
            filename);
      }
      catch (Exception e) {
        getLogger().warn("Unable to store the plan: " + e.getMessage());
      }
      return filename;
    }
    return null;
  }

  public static void lightConfiguration(Configuration cfg) {
    for (Node n : ConfigurationUtils.currentlyOverloadedNodes(cfg)) {
      // get the amount of the overload
      int over = -n.getCPUCapacity();
      for (VirtualMachine vm : cfg.getRunnings(n)) {
        over += vm.getCPUConsumption();
      }

      // Reduce the VMs
      while (over > 0) {
        for (int i = 0; i < cfg.getRunnings(n).size(); i++) {
          VirtualMachine vm = cfg.getRunnings(n).get(i);
          int step = over / 10 + 1;
          if (vm.getCPUConsumption() > step) {
            over -= vm.getCPUConsumption();
            vm.setCPUConsumption(vm.getCPUConsumption() - step);
            over += vm.getCPUConsumption();
          }
          if (over <= 0) {
            break;
          }
        }
      }
    }
  }

  /** Set if the loop compute and perform the reconfiguration or stay idle.
   * @param allow true to allow reconfiguration */
  public void allowReconfiguration(boolean allow) {
    autoReconf = allow;
  }

  /** Indicates wether the control loop auto-reconfigure the infrastructure or
   * not.
   * @return true if reconfiguration are computed then performed */
  public boolean isAllowedToReconfigure() {
    return autoReconf;
  }
}
