/**
 * <h1>Concurrency Control Overview</h1>
 * 
 * The Concurrency Control supports writing collaborations of multiple 
 * users. All participants of a Saros session shall have a consistent 
 * copy of all shared resources.
 * 
 * The Concurrency Control comprises of four subpackages:
 * 
 * <ul>
 * <li>jupiter --- this package contains the Jupiter Algorithm used 
 * for collaborative text editing</li>
 * 
 * <li>management --- manages all JupiterActivities (operations 
 * processed by the users of a Saros session with WRITE_ACCESS)</li>
 * 
 * <li>watchdog --- since inconsistencies might appear the 
 * Consistency Watchdog takes care of them</li>
 * 
 * <li>undo (not in core yet) --- package for undo/redo operations within a Saros 
 * session</li>
 * </ul>
 */
package de.fu_berlin.inf.dpp.concurrent;

