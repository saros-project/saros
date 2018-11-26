package de.fu_berlin.inf.dpp.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/** Describes different types of transfers used to share projects */
@XStreamAlias("TT")
public enum TransferType {
  ARCHIVE,
  INSTANT
}
