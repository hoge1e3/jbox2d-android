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
 * Created at 3:09:27 AM Jul 17, 2010
 */
package org.jbox2d.testbed.framework.android;



import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.pooling.arrays.FloatArray;
import org.jbox2d.pooling.arrays.IntArray;
import org.jbox2d.pooling.arrays.Vec2Array;
import org.jbox2d.testbed.pooling.ColorPool;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

// pooling local, not thread-safe
/**
 * Implementation of {@link DebugDraw} that uses Java2D! Hooray!</br>
 *
 * @author Daniel Murphy
 */
public class DebugDrawAndroid extends DebugDraw {
  public static int circlePoints = 13;

  private final TestPanelAndroid panel;
  private final ColorPool cpool = new ColorPool();

  /**
   * @param viewport
   */
  public DebugDrawAndroid(TestPanelAndroid argTestPanel) {
    super(new OBBViewportTransform());
    viewportTransform.setYFlip(true);
    panel = argTestPanel;
  }

  private final Vec2Array vec2Array = new Vec2Array();

  @Override
  public void drawCircle(Vec2 center, float radius, Color3f color) {
	  Log.d("dd","Drawcir");
    Vec2[] vecs = vec2Array.get(circlePoints);
    generateCirle(center, radius, vecs, circlePoints);
    drawPolygon(vecs, circlePoints, color);
  }

  @Override
  public void drawPoint(Vec2 argPoint, float argRadiusOnScreen, Color3f argColor) {
	  Log.d("dd","DrawP");
getWorldToScreenToOut(argPoint, sp1);
    Canvas g = getGraphics();
    if (g==null) return;

    int c = cpool.getColor(argColor.x, argColor.y, argColor.z);
    Paint p = new Paint();
    p.setColor(c);
    //g.setColor(c);
    sp1.x -= argRadiusOnScreen;
    sp1.y -= argRadiusOnScreen;
    g.drawOval(new RectF(sp1.x, sp1.y, argRadiusOnScreen * 2, argRadiusOnScreen * 2), p);
  }

  private final Vec2 sp1 = new Vec2();
  private final Vec2 sp2 = new Vec2();

  @Override
  public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
    Canvas g = getGraphics();
    if (g==null) return;
	 // Log.d("dd","DrawSeg");
  getWorldToScreenToOut(p1, sp1);
    getWorldToScreenToOut(p2, sp2);


    int c = cpool.getColor(color.x, color.y, color.z);
    Paint p = new Paint();
    p.setColor(c);

    g.drawLine((int) sp1.x, (int) sp1.y, (int) sp2.x, (int) sp2.y , p);
  }

  public void drawAABB(AABB argAABB, Color3f color) {
	  //Log.d("dd","DrawAABB");
    Vec2 vecs[] = vec2Array.get(4);
    argAABB.getVertices(vecs);
    drawPolygon(vecs, 4, color);
  }

  private final Vec2 saxis = new Vec2();

  @Override
  public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
//	  Log.d("dd","DrawSC");
  Vec2[] vecs = vec2Array.get(circlePoints);
    generateCirle(center, radius, vecs, circlePoints);
    drawSolidPolygon(vecs, circlePoints, color);
    if (axis != null) {
      saxis.set(axis).mulLocal(radius).addLocal(center);
      drawSegment(center, saxis, color);
    }
  }

  // TODO change IntegerArray to a specific class for int[] arrays
  private final Vec2 temp = new Vec2();
  //private final static IntArray xIntsPool = new IntArray();
  //private final static IntArray yIntsPool = new IntArray();
  private final static FloatArray xyIntsPool = new FloatArray();

  @Override
  public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
	//  Log.d("dd","DrawSP");

    // inside
    Canvas g = getGraphics();
    if (g==null) return;

    //int[] xInts = xIntsPool.get(vertexCount);
    //int[] yInts = yIntsPool.get(vertexCount);
    float[] xyFloats = xyIntsPool.get(vertexCount*2);


    for (int i = 0; i < vertexCount; i++) {
      getWorldToScreenToOut(vertices[i], temp);
      //xInts[i] = (int) temp.x;
      //yInts[i] = (int) temp.y;
      xyFloats[i*2]  = temp.x;
      xyFloats[i*2+1]= temp.y;
    }

    int c = cpool.getColor(color.x, color.y, color.z, .4f);
    Paint p = new Paint();
    p.setColor(c);
    g.drawLines(xyFloats,p);

    // outside
    drawPolygon(vertices, vertexCount, color);
  }

  @Override
  public void drawString(float x, float y, String s, Color3f color) {
//	  Log.d("dd","DrawStr");
    Canvas g = getGraphics();
    if (g==null) return;

    int c = cpool.getColor(color.x, color.y, color.z);
    Paint p = new Paint();
    p.setColor(c);
    g.drawText(s, x, y,p);
  }

  private Canvas getGraphics() {
    return panel.getDBGraphics();
  }

  private final Vec2 temp2 = new Vec2();

  @Override
  public void drawTransform(Transform xf) {
//	  Log.d("dd","DrawTr");
    Canvas g = getGraphics();
    if (g==null) return;

    getWorldToScreenToOut(xf.position, temp);
    temp2.setZero();
    float k_axisScale = 0.4f;

    int c = cpool.getColor(1, 0, 0);
    Paint p = new Paint();
    p.setColor(c);

    temp2.x = xf.position.x + k_axisScale * xf.R.col1.x;
    temp2.y = xf.position.y + k_axisScale * xf.R.col1.y;
    getWorldToScreenToOut(temp2, temp2);
    g.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y ,p);

    c = cpool.getColor(0, 1, 0);
    p.setColor(c);
    temp2.x = xf.position.x + k_axisScale * xf.R.col2.x;
    temp2.y = xf.position.y + k_axisScale * xf.R.col2.y;
    getWorldToScreenToOut(temp2, temp2);
    g.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y,p);
  }

  // CIRCLE GENERATOR

  private void generateCirle(Vec2 argCenter, float argRadius, Vec2[] argPoints, int argNumPoints) {
    float inc = MathUtils.TWOPI / argNumPoints;

    for (int i = 0; i < argNumPoints; i++) {
      argPoints[i].x = (argCenter.x + MathUtils.cos(i * inc) * argRadius);
      argPoints[i].y = (argCenter.y + MathUtils.sin(i * inc) * argRadius);
    }
  }
}
