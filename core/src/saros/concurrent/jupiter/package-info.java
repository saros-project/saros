/**
 *
 *
 * <h1>Jupiter Algorithm Overview</h1>
 *
 * The Jupiter Architecture is the central concept for Distributed Party Programming in Saros. Each
 * client administers its own copy of a shared artifact (resource). Local {@link Operation}s will be
 * performed immediately and communicated to a central server. From there all other clients get
 * informed about the operation which they will transform in order to perform it on their own
 * copies.
 *
 * <p>The transformation is needed to keep all copies consistent. Consistency maintenance is made up
 * of three components:
 *
 * <ul>
 *   <li>Convergence --- all copies of a document have the same final state after performing a set
 *       of operations to it
 *   <li>Causality-preservation --- for each pair of operations O1 and O2 applies that if O1
 *       precedes O2 then the execution of O1 must take place prior to the execution of O2 for all
 *       copies
 *   <li>Intention-preservation --- for each operation O the result must be identical to the
 *       original intent when performing O for all copies. The result may not affect the results of
 *       other independent operation
 * </ul>
 *
 * Important components of the Jupiter package:
 *
 * <ul>
 *   <li>{@link internal.Jupiter} is the central class of the Jupiter Algorithm
 *   <li>{@link internal.text.GOTOInclusionTransformation} is an implementation of the
 *       transformation function of the Jupiter Algorithm
 * </ul>
 */
package de.fu_berlin.inf.dpp.concurrent.jupiter;
