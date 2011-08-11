/**
 * <h1>Concurrency Control Overview</h1>
 * 
 * The Concurrency Control supports writing collaborations of multiple 
 * users. All participants of a Saros session shall have a consistent 
 * copy of all shared files and folders.
 * 
 * The Concurrency Control comprises of four subpackages:
 * 
 * <ul>
 * <li>jupiter --- this package contains the Jupiter Algorithm used 
 * for collaborative text editing</li>
 * 
 * <li>management --- manages all JupiterActivities (operations 
 * processed by the users of a Saros session with 
 * {@link de.fu_berlin.inf.dpp.User.Permission#WRITE_ACCESS})</li>
 * 
 * <li>undo --- package for undo/redo operations within a Saros 
 * session</li>
 * 
 * <li>watchdog --- since inconsistencies might appear the 
 * Consistency Watchdog takes care of them</li>
 * </ul>
 */
package de.fu_berlin.inf.dpp.concurrent;