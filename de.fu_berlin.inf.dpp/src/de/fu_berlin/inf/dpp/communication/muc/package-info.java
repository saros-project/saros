/**
 * <h1>Muc Overview</h1>
 * 
 * When a Saros session is established, all participants are also added to 
 * a common chatroom, the Saros multi user chat (MUC).
 * The MUC package contains classes for realizing the Saros MUC.
 * 
 *
 * <h2>Subpackages</h2>
 * <ul>
 *  <li>events - contains a listener interface and an adapter for handling muc events</li>
 *  <li>negotiation - stores and transmits the session preferences to all other participants</li>
 *  <li>session - realizes a muc session</li>
 *  <li>singleton - the singleton handling the single instance of a {@link MUCSession}</li>
 * </ul>
 *
 *
 */
package de.fu_berlin.inf.dpp.communication.audio.muc;