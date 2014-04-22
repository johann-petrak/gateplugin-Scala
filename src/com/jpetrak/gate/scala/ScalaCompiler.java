package com.jpetrak.gate.scala;

public interface ScalaCompiler {
  public void init();
  public ScalaScript compile(String source);
}
