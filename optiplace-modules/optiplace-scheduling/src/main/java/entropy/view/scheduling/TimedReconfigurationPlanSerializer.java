/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.view.scheduling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import entropy.view.scheduling.action.Deploy;
import entropy.view.scheduling.action.Instantiate;
import entropy.view.scheduling.action.Migration;
import entropy.view.scheduling.action.Pause;
import entropy.view.scheduling.action.Resume;
import entropy.view.scheduling.action.Run;
import entropy.view.scheduling.action.Shutdown;
import entropy.view.scheduling.action.Startup;
import entropy.view.scheduling.action.Stop;
import entropy.view.scheduling.action.Suspend;
import entropy.view.scheduling.action.UnPause;
import fr.emn.optiplace.solver.ReconfigurationResult;

/**
 * Basic interface to specify a serializer of a
 * entropy.plan.ReconfigurationResult.
 * 
 * @author Fabien Hermenier
 */
public interface TimedReconfigurationPlanSerializer {

	/**
	 * Un-serialize a plan.
	 * 
	 * @param in
	 *            the stream to read.
	 * @return the plan
	 * @throws java.io.IOException
	 *             if an error occurred while reading the stream
	 * @throws TimedReconfigurationPlanSerializerException
	 *             if an error occurred while parsing datas
	 */
	ReconfigurationResult unSerialize(InputStream in) throws IOException,
			TimedReconfigurationPlanSerializerException;

	/**
	 * Serialize a Configuration into a stream.
	 * 
	 * @param plan
	 *            the plan to serialize
	 * @param out
	 *            the stream to write to
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(ReconfigurationResult plan, OutputStream out)
			throws IOException;

	/**
	 * Serialize a migration action that is a part of the current serialized
	 * plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Migration a) throws IOException;

	/**
	 * Serialize a run action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Run a) throws IOException;

	/**
	 * Serialize a stop action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Stop a) throws IOException;

	/**
	 * Serialize a suspend action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Suspend a) throws IOException;

	/**
	 * Serialize a resume action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Resume a) throws IOException;

	/**
	 * Serialize a startup action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Startup a) throws IOException;

	/**
	 * Serialize a shutdown action that is a part of the current serialized
	 * plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Shutdown a) throws IOException;

	/**
	 * Serialize a pause action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Pause a) throws IOException;

	/**
	 * Serialize a unpause action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(UnPause a) throws IOException;

	/**
	 * Serialize an instantiate action that is a part of the current serialized
	 * plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Instantiate a) throws IOException;

	/**
	 * Serialize a deploy action that is a part of the current serialized plan.
	 * 
	 * @param a
	 *            the action to serialize
	 * @throws IOException
	 *             if an error occurred while writing to the stream
	 */
	void serialize(Deploy a) throws IOException;
}
