/*
 * Copyright (c) 2010- Austrian Research Institute for Artificial Intelligence (OFAI). 
 * Copyright (C) 2014-2016 The University of Sheffield.
 *
 * This file is part of gateplugin-ModularPipelines
 * (see https://github.com/johann-petrak/gateplugin-ModularPipelines)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpetrak.gate.scala;

import gate.util.GateClassLoader;

public interface ScalaCompiler {
  public void init();
  /**
   * Compile a script.
   * 
   * The name should be the actual class name used for this compilation. 
   * the classloader should be a classloader that should get used to ultimately
   * load the class into.
   *
   * @param name
   * @param source
   * @param classloader
   * @return 
   */
  public ScalaScript compile(String name, String source, GateClassLoader classloader);
  
  public String getClassEpilog();
  
}
