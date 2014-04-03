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
package entropy;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.controlLoop.ControlLoop;

/** This class aims at launching entropy.
 * @author Fabien Hermenier */
public final class Entropy extends Thread implements EntropyMBean {

  /** The logger associated to entropy. */
  private static final Logger LOGGER = LoggerFactory.getLogger("Entropy");

  /** The name of the mbean. */
  public static final String MBEAN_NAME = "entropy:type=controlLoop";

  /** Number of milliseconds in one second. */
  public static final long SECONDS = 1000L;

  /** The sleep delay in seconds between two iterations. */
  private int sleepDelay;

  /** The control loop tp */
  private ControlLoop controlLoop;

  /** The connector to link the MBean to the RMI registry. */
  private JMXConnectorServer cs;

  /** The registry port. */
  private int port;

  /** Indicates if the control loop has to terminate or not. */
  private Boolean hasToShutdown = false;

  /** The lock used to wait while requesting a shutdown. */
  private final Object stopLock;

  /** The lock used to get the state of entropy. */
  private final Object stateLock;

  /** Indicates wether Entropy is running or not */
  private boolean isRunning;

  /** The thread that run the control loop. */
  private Thread t;

  /** Launch Entropy with a specific control loop.
   * @param loop the control loop to use */
  public Entropy(ControlLoop loop) {
    controlLoop = loop;
    stateLock = new Object();
    isRunning = false;
    stopLock = new Object();
  }

  /** Get the logger.
   * @return an initialized loger */
  public static Logger getLogger() {
    return LOGGER;
  }

  /** Set the binding port of the registry.
   * @param p the port */
  public void setRegistryPort(int p) {
    port = p;
  }

  /** Get the binding port of the registry.
   * @return the port */
  public int getRegistryPort() {
    return port;
  }

  /** Set the sleep delay between two iterations.
   * @param sec the delay in seconds. */
  public void setSleepDelay(int sec) {
    sleepDelay = sec;
  }

  /** Get the sleep delay between two iterations.
   * @return a delay in seconds */
  public int getSleepDelay() {
    return sleepDelay;
  }

  /** Indicates wether Entropy must exit or not.
   * @return true if the control loop demand to exit */
  protected boolean mustExit() {
    boolean ret;
    synchronized (stopLock) {
      ret = hasToShutdown;
    }
    return ret;
  }

  @Override
  public void shutdown() throws EntropyException {
    if (!isRunning()) {
      throw new EntropyException("ControlLoop is not running");
    }
    LOGGER.debug("Waiting for the stoplock");
    synchronized (stopLock) {
      hasToShutdown = Boolean.TRUE;
    }
    LOGGER.debug("Releasing the stoplock");
    try {
      cs.stop();
      MBeanServer srv = ManagementFactory.getPlatformMBeanServer();
      srv.unregisterMBean(new ObjectName(MBEAN_NAME));
      LOGGER.info("Control loop is no more binded");
      // check or tests fail
      if (t != null) {
        t.join();
      }
      synchronized (stateLock) {
        isRunning = false;
      }
    }
    catch (IOException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (InterruptedException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (MalformedObjectNameException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (InstanceNotFoundException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (MBeanRegistrationException e) {
      throw new EntropyException(e.getMessage(), e);
    }
  }

  /** Check wether Entropy is running or not
   * @return true if Entropy is running */
  public boolean isRunning() {
    boolean b;
    synchronized (stateLock) {
      b = isRunning;
    }
    return b;
  }

  /** Start a control loop in another thread and register Entropy to allow a
   * remote shutdown
   * @throws EntropyException if an error occurred while starting the loop. */
  public void startup() throws EntropyException {

    Registry registry;
    try {
      registry = LocateRegistry.getRegistry(port);
      // This registry is not necessarily running
      // to really check the access to the registry
      // we perform a list that fail if the access is not OK
      // (if there is no existing registry)
      registry.list();
      LOGGER.debug("Using existing RMI registry on localhost:" + port);
    }
    catch (RemoteException e) {
      LOGGER.debug("Creating a RMI registry on localhost:" + port);
      try {
        LocateRegistry.createRegistry(port);
      }
      catch (RemoteException e1) {
        throw new EntropyException(e1.getMessage(), e1);
      }
    }
    MBeanServer srv = ManagementFactory.getPlatformMBeanServer();
    try {
      srv.registerMBean(this, new ObjectName(MBEAN_NAME));
      String url = "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/"
          + MBEAN_NAME;
      JMXServiceURL serviceURL = new JMXServiceURL(url);
      cs = JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, null,
          srv);
      cs.start();
      LOGGER.info("Control loop binded on the following URL: " + url);
    }
    catch (IOException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (NotCompliantMBeanException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (InstanceAlreadyExistsException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (MalformedObjectNameException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    catch (MBeanRegistrationException e) {
      throw new EntropyException(e.getMessage(), e);
    }
    synchronized (stateLock) {
      isRunning = true;
    }
    start();
  }

  /** Inifinite loop in the separated thread. */
  @Override
  public void run() {
    t = Thread.currentThread();
    LOGGER.info("Infinite loop started " + Thread.currentThread());
    while (!mustExit()) {
      LOGGER.debug("Starting a new loop iteration");
      synchronized (stopLock) {
        controlLoop.runLoop();
      }
      if (mustExit()) {
        controlLoop.destroy();
        break;
      }
      LOGGER.debug("Waiting ...");
      try {
        Thread.sleep(sleepDelay * SECONDS);
      }
      catch (InterruptedException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
    LOGGER.info("Infinite loop ended");
  }
}
