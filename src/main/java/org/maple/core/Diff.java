package org.maple.core;

public class Diff {
  public LinkedList<Rule> removed;  // = old - new
  public LinkedList<Rule> added;    // = new - old

  public Diff (LinkedList<Rule> removed, LinkedList<Rule> added) {
    this.removed = removed;
    this.added   = added;
  }
}
