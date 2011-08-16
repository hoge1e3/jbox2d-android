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
 * Created at 2:21:03 PM Jul 17, 2010
 */
package org.jbox2d.testbed.framework;

import java.util.LinkedList;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.callbacks.DestructionListener;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.Collision.PointState;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Murphy
 */
public abstract class TestbedTest implements ContactListener/*, ObjectListener, ObjectSigner,
    UnsupportedListener*/ {
  public static final int MAX_CONTACT_POINTS = 2048;

  protected static final long GROUND_BODY_TAG = 1897450239847L;
  protected static final long BOMB_TAG = 98989788987L;
  protected static final long MOUSE_JOINT_TAG = 4567893364789L;

  private static final Logger log = LoggerFactory.getLogger(TestbedTest.class);

  // keep these static so we don't have to recreate them every time
  public final static ContactPoint[] points = new ContactPoint[MAX_CONTACT_POINTS];
  static {
    for (int i = 0; i < MAX_CONTACT_POINTS; i++) {
      points[i] = new ContactPoint();
    }
  }

  /**
   * Only visible for compatibility. Should use {@link #getWorld()} instead.
   */
  protected World m_world;
  private Body groundBody;
  private MouseJoint mouseJoint;

  private Body bomb;
  private final Vec2 bombSpawnPoint = new Vec2();
  private boolean bombSpawning = false;

  private final Vec2 mouseWorld = new Vec2();
  private int pointCount;
  private int stepCount;

  private TestbedModel model;
  private DestructionListener destructionListener;

  private final LinkedList<QueueItem> inputQueue;

  private String title = null;
  protected int m_textLine;
  private final LinkedList<String> textList = new LinkedList<String>();

  private float cachedCameraScale;
  private final Vec2 cachedCameraPos = new Vec2();
  private boolean hasCachedCamera = false;

  //private JbSerializer serializer;
  //private JbDeserializer deserializer;

  private boolean dialogOnSaveLoadErrors = true;

  private boolean savePending, loadPending, resetPending = false;

  public TestbedTest() {
    inputQueue = new LinkedList<QueueItem>();
    /*serializer = new PbSerializer(this, new SignerAdapter(this) {
      @Override
      public Long getTag(Body argBody) {
        if (isSaveLoadEnabled()) {
          if (argBody == groundBody) {
            return GROUND_BODY_TAG;
          } else if (argBody == bomb) {
            return BOMB_TAG;
          }
        }
        return super.getTag(argBody);
      }

      @Override
      public Long getTag(Joint argJoint) {
        if (isSaveLoadEnabled()) {
          if (argJoint == mouseJoint) {
            return MOUSE_JOINT_TAG;
          }
        }
        return super.getTag(argJoint);
      }
    });
    deserializer = new PbDeserializer(this, new ListenerAdapter(this) {
      @Override
      public void processBody(Body argBody, Long argTag) {
        if (isSaveLoadEnabled()) {
          if (argTag == GROUND_BODY_TAG) {
            groundBody = argBody;
            return;
          } else if (argTag == BOMB_TAG) {
            bomb = argBody;
            return;
          }
        }
        super.processBody(argBody, argTag);
      }

      @Override
      public void processJoint(Joint argJoint, Long argTag) {
        if (isSaveLoadEnabled()) {
          if (argTag == MOUSE_JOINT_TAG) {
            mouseJoint = (MouseJoint) argJoint;
            return;
          }
        }
        super.processJoint(argJoint, argTag);
      }
    });*/
  }

  public void init(TestbedModel argModel) {
    model = argModel;
    destructionListener = new DestructionListener() {

      public void sayGoodbye(Fixture fixture) {
      }

      public void sayGoodbye(Joint joint) {
        if (mouseJoint == joint) {
          mouseJoint = null;
        } else {
          jointDestroyed(joint);
        }
      }
    };

    Vec2 gravity = new Vec2(0, -10f);
    m_world = new World(gravity, true);
    bomb = null;
    m_textLine = 30;
    mouseJoint = null;

    BodyDef bodyDef = new BodyDef();
    groundBody = m_world.createBody(bodyDef);

    init(m_world, false);
  }

  public void init(World argWorld, boolean argDeserialized) {
    pointCount = 0;
    stepCount = 0;
    bombSpawning = false;

    argWorld.setDestructionListener(destructionListener);
    argWorld.setContactListener(this);
    argWorld.setDebugDraw(model.getDebugDraw());

    if (hasCachedCamera) {
      setCamera(cachedCameraPos, cachedCameraScale);
    } else {
      setCamera(getDefaultCameraPos(), getDefaultCameraScale());
    }
    setTitle(getTestName());

    initTest(argDeserialized);
  }

  public World getWorld() {
    return m_world;
  }

  public TestbedModel getModel() {
    return model;
  }

  public Body getGroundBody() {
    return groundBody;
  }

  public DebugDraw getDebugDraw() {
    return model.getDebugDraw();
  }

  public Vec2 getWorldMouse() {
    return mouseWorld;
  }

  public int getStepCount() {
    return stepCount;
  }

  public int getPointCount() {
    return pointCount;
  }

  public Body getBomb() {
    return bomb;
  }

  public float getCachedCameraScale() {
    return cachedCameraScale;
  }

  public void setCachedCameraScale(float cachedCameraScale) {
    this.cachedCameraScale = cachedCameraScale;
  }

  public Vec2 getCachedCameraPos() {
    return cachedCameraPos;
  }

  public void setCachedCameraPos(Vec2 argPos) {
    cachedCameraPos.set(argPos);
  }

  public boolean isHasCachedCamera() {
    return hasCachedCamera;
  }

  public void setHasCachedCamera(boolean hasCachedCamera) {
    this.hasCachedCamera = hasCachedCamera;
  }

  public boolean isDialogOnSaveLoadErrors() {
    return dialogOnSaveLoadErrors;
  }

  public void setDialogOnSaveLoadErrors(boolean dialogOnSaveLoadErrors) {
    this.dialogOnSaveLoadErrors = dialogOnSaveLoadErrors;
  }

  /**
   * Override for a different default camera pos
   *
   * @return
   */
  public Vec2 getDefaultCameraPos() {
    return new Vec2(0, 10);
  }

  /**
   * Override for a different default camera scale
   *
   * @return
   */
  public float getDefaultCameraScale() {
    return 10;
  }

  public String getFilename() {
    return getTestName().toLowerCase().replaceAll(" ", "_") + ".box2d";
  }

  public void reset() {
    resetPending = true;
  }

  public void save() {
    savePending = true;
  }

  public void load() {
    loadPending = true;
  }

  protected void _reset() {
    init(model);
  }

  protected void _save() {

    /*SerializationResult result;
    try {
      result = serializer.serialize(m_world);
    } catch (UnsupportedObjectException e1) {
      log.error("Error serializing world", e1);
      if (dialogOnSaveLoadErrors) {
        JOptionPane.showConfirmDialog(null, "Error serializing the object: " + e1.toString(),
            "Serialization Error", JOptionPane.OK_OPTION);
      }
      return;
    }

    try {
      FileOutputStream fos = new FileOutputStream(getFilename());
      result.writeTo(fos);
    } catch (FileNotFoundException e) {
      log.error("File not found exception while saving", e);
      if (dialogOnSaveLoadErrors) {
        JOptionPane.showConfirmDialog(null, "File not found exception while saving: "
            + getFilename(), "Serialization Error", JOptionPane.OK_OPTION);
      }
      return;
    } catch (IOException e) {
      log.error("Exception while writing world", e);
      if (dialogOnSaveLoadErrors) {
        JOptionPane.showConfirmDialog(null, "Error while writing world: " + e.toString(),
            "Serialization Error", JOptionPane.OK_OPTION);
      }
      return;
    }
    return;*/
  }

  protected void _load() {
/*
    World w;
    try {
      FileInputStream fis = new FileInputStream(getFilename());
      w = deserializer.deserializeWorld(fis);
    } catch (FileNotFoundException e) {
      log.error("File not found error while loading", e);
      if (dialogOnSaveLoadErrors) {
        JOptionPane.showMessageDialog(null, "File not found exception while loading: "
            + getFilename(), "Serialization Error", JOptionPane.ERROR_MESSAGE);
      }
      return;
    } catch (UnsupportedObjectException e) {
      log.error("Error deserializing world", e);
      if (dialogOnSaveLoadErrors) {
        JOptionPane.showMessageDialog(null, "Error serializing the object: " + e.toString(),
            "Serialization Error", JOptionPane.ERROR_MESSAGE);
      }
      return;
    } catch (IOException e) {
      log.error("Exception while writing world", e);
      if (dialogOnSaveLoadErrors) {
        JOptionPane.showMessageDialog(null, "Error while reading world: " + e.toString(),
            "Serialization Error", JOptionPane.ERROR_MESSAGE);
      }
      return;
    }
    m_world = w;

    init(m_world, true);
    return;*/
  }

  public void setCamera(Vec2 argPos, float scale) {
    model.getDebugDraw().setCamera(argPos.x, argPos.y, scale);
    hasCachedCamera = true;
    cachedCameraScale = scale;
    cachedCameraPos.set(argPos);
  }

  public abstract void initTest(boolean argDeserialized);

  public abstract String getTestName();

  public void update() {
    if (resetPending) {
      _reset();
      resetPending = false;
    }
    if (savePending) {
      _save();
      savePending = false;
    }
    if (loadPending) {
      _load();
      loadPending = false;
    }

    m_textLine = 15;

    if (title != null) {
      model.getDebugDraw().drawString(model.getPanelWidth() / 2, 15, title, Color3f.WHITE);
    }

    // process our input
    if (!inputQueue.isEmpty()) {
      synchronized (inputQueue) {
        while (!inputQueue.isEmpty()) {
          QueueItem i = inputQueue.poll();
          switch (i.type) {
            case KeyPressed:
              keyPressed(i.c, i.code);
              break;
            case KeyReleased:
              keyReleased(i.c, i.code);
              break;
            case MouseDown:
              mouseDown(i.p);
              break;
            case MouseMove:
              mouseMove(i.p);
              break;
            case MouseUp:
              mouseUp(i.p);
              break;
            case ShiftMouseDown:
              shiftMouseDown(i.p);
              break;
          }
        }
      }
    }

    step(model.getSettings());
  }

  private final Color3f color1 = new Color3f(.3f, .95f, .3f);
  private final Color3f color2 = new Color3f(.3f, .3f, .95f);
  private final Color3f color3 = new Color3f(.9f, .9f, .9f);
  private final Color3f color4 = new Color3f(.6f, .61f, 1);
  private final Color3f mouseColor = new Color3f(0f, 1f, 0f);
  private final Vec2 p1 = new Vec2();
  private final Vec2 p2 = new Vec2();

  public synchronized void step(TestbedSettings settings) {
    float hz = settings.getSetting(TestbedSettings.Hz).value;
    float timeStep = hz > 0f ? 1f / hz : 0;

    if (settings.singleStep && !settings.pause) {
      settings.pause = true;
    }

    if (settings.pause) {
      if (settings.singleStep) {
        settings.singleStep = false;
      } else {
        timeStep = 0;
      }

      model.getDebugDraw().drawString(5, m_textLine, "****PAUSED****", Color3f.WHITE);
      m_textLine += 15;
    }

    int flags = 0;
    model.getDebugDraw();
    flags += settings.getSetting(TestbedSettings.DrawShapes).enabled ? DebugDraw.e_shapeBit : 0;
    flags += settings.getSetting(TestbedSettings.DrawJoints).enabled ? DebugDraw.e_jointBit : 0;
    flags += settings.getSetting(TestbedSettings.DrawAABBs).enabled ? DebugDraw.e_aabbBit : 0;
    flags += settings.getSetting(TestbedSettings.DrawPairs).enabled ? DebugDraw.e_pairBit : 0;
    flags += settings.getSetting(TestbedSettings.DrawCOMs).enabled ? DebugDraw.e_centerOfMassBit
        : 0;
    flags += settings.getSetting(TestbedSettings.DrawTree).enabled ? DebugDraw.e_dynamicTreeBit : 0;
    model.getDebugDraw().setFlags(flags);

    m_world.setWarmStarting(settings.getSetting(TestbedSettings.WarmStarting).enabled);
    m_world.setContinuousPhysics(settings.getSetting(TestbedSettings.ContinuousCollision).enabled);

    pointCount = 0;

    m_world.step(timeStep, settings.getSetting(TestbedSettings.VelocityIterations).value,
        settings.getSetting(TestbedSettings.PositionIterations).value);

    draw(settings, timeStep);
  }

  private void draw(TestbedSettings settings, float timeStep) {
	  m_world.drawDebugData();

	  if (timeStep > 0f) {
		  ++stepCount;
	  }

	  if (settings.getSetting(TestbedSettings.DrawStats).enabled) {
		  // Vec2.watchCreations = true;
		  model.getDebugDraw().drawString(5, m_textLine, "Engine Info", color4);
		  m_textLine += 15;
		  model.getDebugDraw().drawString(5, m_textLine,
				  "Framerate: " + model.getCalculatedFps(), Color3f.WHITE);
		  m_textLine += 15;
		  model.getDebugDraw().drawString(
				  5,
				  m_textLine,
				  "bodies/contacts/joints/proxies = " + m_world.getBodyCount() + "/"
				  + m_world.getContactCount() + "/" + m_world.getJointCount() + "/"
				  + m_world.getProxyCount(), Color3f.WHITE);
		  // m_textLine += 20;
		  // m_model.getDebugDraw().drawString(5, m_textLine, "Pooling Info", color4);
		  // m_textLine += 15;
		  // m_model.getDebugDraw().drawString(5, m_textLine, "Vec2 creations: "+ Vec2.creationCount,
		  // Color3f.WHITE);
		  // m_textLine += 15;
		  // m_model.getDebugDraw().drawString(5, m_textLine, "Contact pooled/active: "+
		  // Contact.contactPoolCount+"/"+Contact.activeContacts, Color3f.WHITE);
		  m_textLine += 20;

		  // Vec2.creationCount = 0;
	  } else {
		  // Vec2.watchCreations = false;
	  }

	  if (settings.getSetting(TestbedSettings.DrawHelp).enabled) {
		  model.getDebugDraw().drawString(5, m_textLine, "Help", color4);
		  m_textLine += 15;
		  model.getDebugDraw().drawString(5, m_textLine,
				  "Click and drag the left mouse button to move objects.", Color3f.WHITE);
		  m_textLine += 15;
		  model.getDebugDraw().drawString(5, m_textLine,
				  "Shift-Click to aim a bullet, or press space.", Color3f.WHITE);
		  m_textLine += 15;
		  model.getDebugDraw().drawString(5, m_textLine,
				  "Click and drag the right mouse button to move the view.", Color3f.WHITE);
		  m_textLine += 15;
		  model.getDebugDraw().drawString(5, m_textLine, "Scroll to zoom in/out.", Color3f.WHITE);
		  m_textLine += 15;
		  model.getDebugDraw().drawString(5, m_textLine,
				  "Press '[' or ']' to change tests, and 'r' to restart.", Color3f.WHITE);
		  m_textLine += 20;
	  }

	  if (!textList.isEmpty()) {
		  model.getDebugDraw().drawString(5, m_textLine, "Test Info", color4);
		  m_textLine += 15;
		  for (String s : textList) {
			  model.getDebugDraw().drawString(5, m_textLine, s, Color3f.WHITE);
			  m_textLine += 15;
		  }
		  textList.clear();
	  }

	  if (mouseJoint != null) {
		  mouseJoint.getAnchorB(p1);
		  Vec2 p2 = mouseJoint.getTarget();

		  model.getDebugDraw().drawSegment(p1, p2, mouseColor);
	  }

	  if (bombSpawning) {
		  model.getDebugDraw().drawSegment(bombSpawnPoint, mouseWorld, Color3f.WHITE);
	  }

	  if (settings.getSetting(TestbedSettings.DrawContactPoints).enabled) {
		  final float axisScale = .3f;

		  for (int i = 0; i < pointCount; i++) {

			  ContactPoint point = points[i];

			  if (point.state == PointState.ADD_STATE) {
				  model.getDebugDraw().drawPoint(point.position, 10f, color1);
			  } else if (point.state == PointState.PERSIST_STATE) {
				  model.getDebugDraw().drawPoint(point.position, 5f, color2);
			  }

			  if (settings.getSetting(TestbedSettings.DrawNormals).enabled) {
				  p1.set(point.position);
				  p2.set(point.normal).mulLocal(axisScale).addLocal(p1);
				  model.getDebugDraw().drawSegment(p1, p2, color3);
			  }
		  }
	  }
  }

  public void queueShiftMouseDown(Vec2 p) {
    synchronized (inputQueue) {
      inputQueue.addLast(new QueueItem(QueueItemType.ShiftMouseDown, p));
    }
  }

  public void queueMouseUp(Vec2 p) {
    synchronized (inputQueue) {
      inputQueue.addLast(new QueueItem(QueueItemType.MouseUp, p));
    }
  }

  public void queueMouseDown(Vec2 p) {
    synchronized (inputQueue) {
      inputQueue.addLast(new QueueItem(QueueItemType.MouseDown, p));
    }
  }

  public void queueMouseMove(Vec2 p) {
    synchronized (inputQueue) {
      inputQueue.addLast(new QueueItem(QueueItemType.MouseMove, p));
    }
  }

  public void queueKeyPressed(char c, int code) {
    synchronized (inputQueue) {
      inputQueue.addLast(new QueueItem(QueueItemType.KeyPressed, c, code));
    }
  }

  public void queueKeyReleased(char c, int code) {
    synchronized (inputQueue) {
      inputQueue.addLast(new QueueItem(QueueItemType.KeyReleased, c, code));
    }
  }

  public void shiftMouseDown(Vec2 p) {
    mouseWorld.set(p);

    if (mouseJoint != null) {
      return;
    }

    spawnBomb(p);
  }

  public void mouseUp(Vec2 p) {
    if (mouseJoint != null) {
      m_world.destroyJoint(mouseJoint);
      mouseJoint = null;
    }

    if (bombSpawning) {
      completeBombSpawn(p);
    }
  }

  private final AABB queryAABB = new AABB();
  private final TestQueryCallback callback = new TestQueryCallback();

  public void mouseDown(Vec2 p) {
    mouseWorld.set(p);

    if (mouseJoint != null) {
      return;
    }

    queryAABB.lowerBound.set(p.x - .001f, p.y - .001f);
    queryAABB.upperBound.set(p.x + .001f, p.y + .001f);
    callback.point.set(p);
    callback.fixture = null;
    m_world.queryAABB(callback, queryAABB);

    if (callback.fixture != null) {
      Body body = callback.fixture.getBody();
      MouseJointDef def = new MouseJointDef();
      def.bodyA = groundBody;
      def.bodyB = body;
      def.target.set(p);
      def.maxForce = 1000f * body.getMass();
      mouseJoint = (MouseJoint) m_world.createJoint(def);
      body.setAwake(true);
    }
  }

  public void mouseMove(Vec2 p) {
    mouseWorld.set(p);

    if (mouseJoint != null) {
      mouseJoint.setTarget(p);
    }
  }

  public void setTitle(String argTitle) {
    title = argTitle;
  }

  public void addTextLine(String argTextLine) {
    textList.add(argTextLine);
  }

  private final Vec2 p = new Vec2();
  private final Vec2 v = new Vec2();

  public void lanchBomb() {
    p.set((float) (Math.random() * 30 - 15), 30f);
    v.set(p).mulLocal(-5f);
    launchBomb(p, v);
  }

  private final AABB aabb = new AABB();

  public synchronized void launchBomb(Vec2 position, Vec2 velocity) {
    if (bomb != null) {
      m_world.destroyBody(bomb);
      bomb = null;
    }
    // todo optimize this
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(position);
    bd.bullet = true;
    bomb = m_world.createBody(bd);
    bomb.setLinearVelocity(velocity);

    CircleShape circle = new CircleShape();
    circle.m_radius = 0.3f;

    FixtureDef fd = new FixtureDef();
    fd.shape = circle;
    fd.density = 20f;
    fd.restitution = 0;

    Vec2 minV = new Vec2(position);
    Vec2 maxV = new Vec2(position);

    minV.subLocal(new Vec2(.3f, .3f));
    maxV.addLocal(new Vec2(.3f, .3f));

    aabb.lowerBound.set(minV);
    aabb.upperBound.set(maxV);

    bomb.createFixture(fd);
  }

  public synchronized void spawnBomb(Vec2 worldPt) {
    bombSpawnPoint.set(worldPt);
    bombSpawning = true;
  }

  private final Vec2 vel = new Vec2();

  public synchronized void completeBombSpawn(Vec2 p) {
    if (bombSpawning == false) {
      return;
    }

    float multiplier = 30f;
    vel.set(bombSpawnPoint).subLocal(p);
    vel.mulLocal(multiplier);
    launchBomb(bombSpawnPoint, vel);
    bombSpawning = false;
  }

  /**
   * Override to enable saving and loading. Remember to also override the {@link ObjectListener} and
   * {@link ObjectSigner} methods if you need to
   *
   * @return
   */
  public boolean isSaveLoadEnabled() {
    return false;
  }

  /*@Override
  public Long getTag(Body argBody) {
    return null;
  }

  @Override
  public Long getTag(Fixture argFixture) {
    return null;
  }

  @Override
  public Long getTag(Joint argJoint) {
    return null;
  }

  @Override
  public Long getTag(Shape argShape) {
    return null;
  }

  @Override
  public Long getTag(World argWorld) {
    return null;
  }

  @Override
  public void processBody(Body argBody, Long argTag) {
  }

  @Override
  public void processFixture(Fixture argFixture, Long argTag) {
  }

  @Override
  public void processJoint(Joint argJoint, Long argTag) {
  }

  @Override
  public void processShape(Shape argShape, Long argTag) {
  }

  @Override
  public void processWorld(World argWorld, Long argTag) {
  }

  @Override
  public boolean isUnsupported(UnsupportedObjectException argException) {
    return true;
  }*/

  public void jointDestroyed(Joint joint) {
  }

  public void beginContact(Contact contact) {
  }

  public void endContact(Contact contact) {
  }

  public void postSolve(Contact contact, ContactImpulse impulse) {
  }

  private final PointState[] state1 = new PointState[Settings.maxManifoldPoints];
  private final PointState[] state2 = new PointState[Settings.maxManifoldPoints];
  private final WorldManifold worldManifold = new WorldManifold();

  public void preSolve(Contact contact, Manifold oldManifold) {
    Manifold manifold = contact.getManifold();

    if (manifold.pointCount == 0) {
      return;
    }

    Fixture fixtureA = contact.getFixtureA();
    Fixture fixtureB = contact.getFixtureB();

    Collision.getPointStates(state1, state2, oldManifold, manifold);

    contact.getWorldManifold(worldManifold);

    for (int i = 0; i < manifold.pointCount && pointCount < MAX_CONTACT_POINTS; i++) {
      ContactPoint cp = points[pointCount];
      cp.fixtureA = fixtureA;
      cp.fixtureB = fixtureB;
      cp.position.set(worldManifold.points[i]);
      cp.normal.set(worldManifold.normal);
      cp.state = state2[i];
      ++pointCount;
    }
  }

  public void keyPressed(char argKeyChar, int argKeyCode) {
  }

  public void keyReleased(char argKeyChar, int argKeyCode) {
  }
}

class TestQueryCallback implements QueryCallback {

  public final Vec2 point;
  public Fixture fixture;

  public TestQueryCallback() {
    point = new Vec2();
    fixture = null;
  }

  /**
   * @see org.jbox2d.callbacks.QueryCallback#reportFixture(org.jbox2d.dynamics.Fixture)
   */
  public boolean reportFixture(Fixture argFixture) {
    Body body = argFixture.getBody();
    if (body.getType() == BodyType.DYNAMIC) {
      boolean inside = argFixture.testPoint(point);
      if (inside) {
        fixture = argFixture;

        return false;
      }
    }

    return true;
  }
}

enum QueueItemType {
  MouseDown, MouseMove, MouseUp, ShiftMouseDown, KeyPressed, KeyReleased
}

class QueueItem {
  public QueueItemType type;
  public Vec2 p;
  public char c;
  public int code;

  public QueueItem(QueueItemType t, Vec2 pt) {
    type = t;
    p = pt;
  }

  public QueueItem(QueueItemType t, char cr, int cd) {
    type = t;
    c = cr;
    code = cd;
  }
}

/*class SignerAdapter implements ObjectSigner {
  private final ObjectSigner delegate;

  public SignerAdapter(ObjectSigner argDelegate) {
    delegate = argDelegate;
  }

  public Long getTag(World argWorld) {
    return delegate.getTag(argWorld);
  }

  public Long getTag(Body argBody) {
    return delegate.getTag(argBody);
  }

  public Long getTag(Shape argShape) {
    return delegate.getTag(argShape);
  }

  public Long getTag(Fixture argFixture) {
    return delegate.getTag(argFixture);
  }

  public Long getTag(Joint argJoint) {
    return delegate.getTag(argJoint);
  }
}

class ListenerAdapter implements ObjectListener {
  private final ObjectListener listener;

  public ListenerAdapter(ObjectListener argListener) {
    listener = argListener;
  }

  public void processWorld(World argWorld, Long argTag) {
    listener.processWorld(argWorld, argTag);
  }

  public void processBody(Body argBody, Long argTag) {
    listener.processBody(argBody, argTag);
  }

  public void processFixture(Fixture argFixture, Long argTag) {
    listener.processFixture(argFixture, argTag);
  }

  public void processShape(Shape argShape, Long argTag) {
    listener.processShape(argShape, argTag);
  }

  public void processJoint(Joint argJoint, Long argTag) {
    listener.processJoint(argJoint, argTag);
  }
}*/
