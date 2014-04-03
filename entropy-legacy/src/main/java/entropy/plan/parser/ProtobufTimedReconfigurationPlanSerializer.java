/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.plan.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import entropy.configuration.Configuration;
import entropy.configuration.parser.ProtobufConfigurationSerializer;
import entropy.plan.Defaultentropy.plan.ReconfigurationResult;
import entropy.plan.entropy.plan.ReconfigurationResult;
import entropy.plan.action.*;
import entropy.plan.action.Shutdown;

/** @author Fabien Hermenier */
public class ProtobufTimedReconfigurationPlanSerializer extends
    FileTimedReconfigurationPlanSerializer {

  private final static ProtobufTimedReconfigurationPlanSerializer instance = new ProtobufTimedReconfigurationPlanSerializer();

  private PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Builder planBuilder;

  private ProtobufTimedReconfigurationPlanSerializer() {}

  @Override
  public entropy.plan.ReconfigurationResult unSerialize(InputStream in)
      throws IOException, TimedReconfigurationPlanSerializerException {
    entropy.plan.ReconfigurationResult plan;
    try {
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult p = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult
          .parseFrom(in);
      ProtobufConfigurationSerializer.getInstance();
      Configuration cfg = ProtobufConfigurationSerializer
          .convert(p.getSource());
      plan = new Defaultentropy.plan.ReconfigurationResult(cfg);
      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Migration m : p
          .getMigrationsList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Stop m : p
          .getStopsList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Startup m : p
          .getStartupsList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Run m : p
          .getRunsList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Resume m : p
          .getResumesList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Suspend m : p
          .getSuspendsList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Pause m : p
          .getPausesList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Unpause m : p
          .getUnpausesList()) {
        plan.add(convert(cfg, m));
      }

      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Instantiate m : p
          .getInstantiatesList()) {
        plan.add(convert(cfg, m));
      }

      // Last to prevent application failures due to a node currently
      // hosting a VM
      for (PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Shutdown m : p
          .getShutdownsList()) {
        plan.add(convert(cfg, m));
      }

    }
    catch (Exception e) {
      throw new TimedReconfigurationPlanSerializerException(e);
    }
    return plan;
  }

  @Override
  public void serialize(entropy.plan.ReconfigurationResult plan, OutputStream out)
      throws IOException {
    planBuilder = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult
        .newBuilder();
    planBuilder.setSource(ProtobufConfigurationSerializer.convert(plan
        .getSource()));

    for (Action a : plan) {
      a.serialize(this);
    }
    planBuilder.build().writeTo(out);
  }

  private static Migration convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Migration a) {
    return new Migration(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg
        .getAllNodes().get(a.getIdSrc()), cfg.getAllNodes().get(a.getIdDst()),
        a.getStart(), a.getEnd());
  }

  private static Run convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Run a) {
    return new Run(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg
        .getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
  }

  private static Stop convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Stop a) {
    return new Stop(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg
        .getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
  }

  private static Startup convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Startup a) {
    return new Startup(cfg.getAllNodes().get(a.getIdN()), a.getStart(),
        a.getEnd());
  }

  private static Shutdown convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Shutdown a) {
    return new Shutdown(cfg.getAllNodes().get(a.getIdN()), a.getStart(),
        a.getEnd());
  }

  private static Suspend convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Suspend a) {
    return new Suspend(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg
        .getAllNodes().get(a.getIdSrc()), cfg.getAllNodes().get(a.getIdDst()),
        a.getStart(), a.getEnd());
  }

  private static Resume convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Resume a) {
    return new Resume(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg
        .getAllNodes().get(a.getIdSrc()), cfg.getAllNodes().get(a.getIdDst()),
        a.getStart(), a.getEnd());
  }

  private static Pause convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Pause a) {
    return new Pause(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg
        .getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
  }

  private static UnPause convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Unpause a) {
    return new UnPause(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg
        .getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
  }

  private static Instantiate convert(Configuration cfg,
      PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Instantiate a) {
    return new Instantiate(cfg.getAllVirtualMachines().get(a.getIdVm()),
        a.getStart(), a.getEnd());
  }

  public static ProtobufTimedReconfigurationPlanSerializer getInstance() {
    return instance;
  }

  @Override
  public void serialize(Migration a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Migration.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Migration
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName())
        .setIdSrc(a.getHost().getName()).setIdDst(a.getDestination().getName());
    planBuilder.addMigrations(b.build());
  }

  @Override
  public void serialize(Run a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Run.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Run
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName()).setIdN(a.getHost().getName());
    planBuilder.addRuns(b.build());
  }

  @Override
  public void serialize(Stop a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Stop.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Stop
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName()).setIdN(a.getHost().getName());
    planBuilder.addStops(b.build());
  }

  @Override
  public void serialize(Suspend a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Suspend.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Suspend
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName())
        .setIdSrc(a.getHost().getName()).setIdDst(a.getDestination().getName());
    planBuilder.addSuspends(b.build());
  }

  @Override
  public void serialize(Resume a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Resume.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Resume
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName())
        .setIdSrc(a.getHost().getName()).setIdDst(a.getDestination().getName());
    planBuilder.addResumes(b.build());
  }

  @Override
  public void serialize(Startup a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Startup.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Startup
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdN(a.getNode().getName());
    planBuilder.addStartups(b.build());
  }

  @Override
  public void serialize(Shutdown a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Shutdown.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Shutdown
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdN(a.getNode().getName());
    planBuilder.addShutdowns(b.build());
  }

  @Override
  public void serialize(Pause a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Pause.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Pause
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName()).setIdN(a.getHost().getName());
    planBuilder.addPauses(b.build());
  }

  @Override
  public void serialize(UnPause a) throws IOException {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Unpause.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Unpause
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName()).setIdN(a.getHost().getName());
    planBuilder.addUnpauses(b.build());
  }

  @Override
  public void serialize(Instantiate a) {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Instantiate.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Instantiate
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdVm(a.getVirtualMachine().getName());
    planBuilder.addInstantiates(b.build());
  }

  @Override
  public void serialize(Deploy a) {
    PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Deploy.Builder b = PBentropy.plan.ReconfigurationResult.entropy.plan.ReconfigurationResult.Deploy
        .newBuilder();
    b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
        .setIdN(a.getNode().getName());
    planBuilder.addDeploys(b.build());
  }
}
