/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/**
 * Created at 1:58:18 PM Jul 17, 2010
 */
package org.jbox2d.testbed.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jbox2d.testbed.framework.TestbedSetting.SettingType;

/**
 * Stores all the testbed settings.  Automatically populates default settings.
 * 
 * @author Daniel Murphy
 */
public class TestbedSettings {
  public static final String Hz = "Hz";
  public static final String PositionIterations = "Pos Iters";
  public static final String VelocityIterations = "Vel Iters";
  public static final String WarmStarting = "Warm Starting";
  public static final String ContinuousCollision = "Continuous Collision";
  public static final String DrawShapes = "Draw Shapes";
  public static final String DrawJoints = "Draw Joints";
  public static final String DrawAABBs = "Draw AABBs";
  public static final String DrawPairs = "Draw Pairs";
  public static final String DrawContactPoints = "Draw Contact Points";
  public static final String DrawNormals = "Draw Normals";
  public static final String DrawCOMs = "Draw Center of Mass";
  public static final String DrawStats = "Draw Stats";
  public static final String DrawHelp = "Draw Help";
  public static final String DrawTree = "Draw Dynamic Tree";

  public boolean pause = false;
  public boolean singleStep = false;

  private ArrayList<TestbedSetting> settings;
  private final HashMap<String, TestbedSetting> settingsMap;

  public TestbedSettings() {
    settings = new ArrayList<TestbedSetting>();
    settingsMap = new HashMap<String, TestbedSetting>();
    populateDefaultSettings();
  }

  private void populateDefaultSettings() {
    addSetting(new TestbedSetting(Hz, SettingType.ENGINE, 60, 1, 400));
    addSetting(new TestbedSetting(PositionIterations, SettingType.ENGINE, 3, 0, 100));
    addSetting(new TestbedSetting(VelocityIterations, SettingType.ENGINE, 8, 1, 100));
    addSetting(new TestbedSetting(WarmStarting, SettingType.ENGINE, true));
    addSetting(new TestbedSetting(ContinuousCollision, SettingType.ENGINE, true));
    addSetting(new TestbedSetting(DrawShapes, SettingType.DRAWING, true));
    addSetting(new TestbedSetting(DrawJoints, SettingType.DRAWING, true));
    addSetting(new TestbedSetting(DrawAABBs, SettingType.DRAWING, false));
    addSetting(new TestbedSetting(DrawPairs, SettingType.DRAWING, false));
    addSetting(new TestbedSetting(DrawContactPoints, SettingType.DRAWING, false));
    addSetting(new TestbedSetting(DrawNormals, SettingType.DRAWING, false));
    addSetting(new TestbedSetting(DrawCOMs, SettingType.DRAWING, false));
    addSetting(new TestbedSetting(DrawStats, SettingType.DRAWING, true));
    addSetting(new TestbedSetting(DrawHelp, SettingType.DRAWING, false));
    addSetting(new TestbedSetting(DrawTree, SettingType.DRAWING, false));
  }

  /**
   * Adds a settings to the settings list
   * @param argSetting
   */
  public void addSetting(TestbedSetting argSetting) {
    if (settingsMap.containsKey(argSetting.name)) {
      throw new IllegalArgumentException("Settings already contain a setting with name: "
          + argSetting.name);
    }
    settings.add(argSetting);
    settingsMap.put(argSetting.name, argSetting);
  }

  /**
   * Returns an unmodifiable list of settings
   * @return
   */
  public List<TestbedSetting> getSettings() {
    return Collections.unmodifiableList(settings);
  }

  /**
   * Gets a setting by name.
   * @param argName
   * @return
   */
  public TestbedSetting getSetting(String argName) {
    return settingsMap.get(argName);
  }
}
