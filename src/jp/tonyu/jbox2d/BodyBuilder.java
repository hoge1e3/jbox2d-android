package jp.tonyu.jbox2d;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

public class BodyBuilder {
	BodyDef bd=new BodyDef();
	Body b;
	Shape shape;
	float density;
	World world;
	public BodyBuilder(World w) {
		this.world=w;
	}
	public BodyBuilder pos(double d, double e) {
		return pos(new Vec2((float)d,(float)e));
	}
	public BodyBuilder pos(Vec2 p) {
		bd.position.set(p);
		return this;
	}
	public BodyBuilder shape(Shape s) {
		this.shape=s;
		return this;
	}
	public BodyBuilder density(double d) {
		density=(float)d;
		return this;
	}
	public BodyDef bodyDef() {return bd;}
	public Body body() {
		return b;
	}
	public BodyBuilder type(BodyType t) {
		bd.type=t;
		return this;
	}
	public Body build() {
		b=world.createBody(bd);
		b.createFixture(shape, density);
		return b;
	}
}
