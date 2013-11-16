package com.sturdyhelmetgames.roomforchange.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sturdyhelmetgames.roomforchange.level.Level;
import com.sturdyhelmetgames.roomforchange.level.Level.LevelTile;

public class Entity {

	public Level level;
	public EntityState state;
	public Direction direction = Direction.DOWN;

	public static final float ACCEL_MAX = 2f;
	public static final float VEL_MAX = 0.05f;
	private static final float MIN_WALK_VELOCITY = 0.001f;

	public final Vector2 accel = new Vector2(0f, 0f);
	public final Vector2 vel = new Vector2(0f, 0f);
	public float stateTime = 0f;
	public float width, height;
	public final Rectangle bounds = new Rectangle();
	private Rectangle[] r = { new Rectangle(), new Rectangle(),
			new Rectangle(), new Rectangle() };
	public int[][] tiles;

	public float getMaxVelocity() {
		return VEL_MAX;
	}

	public enum Direction {
		UP, DOWN, LEFT, RIGHT;
	}

	public enum EntityState {
		IDLE, WALKING;
	}

	public Entity(float x, float y, float width, float height, Level level) {
		this.level = level;
		this.width = width;
		this.height = height;
		bounds.set(x, y, width, height);
		state = EntityState.IDLE;
	}

	public void render(float delta, SpriteBatch batch) {

	}

	public void update(float fixedStep) {
		tryMove();

		vel.add(accel);
		if (vel.x > getMaxVelocity()) {
			vel.x = getMaxVelocity();
		}
		if (vel.x < -getMaxVelocity()) {
			vel.x = -getMaxVelocity();
		}
		if (vel.y > getMaxVelocity()) {
			vel.y = getMaxVelocity();
		}
		if (vel.y < -getMaxVelocity()) {
			vel.y = -getMaxVelocity();
		}
		accel.scl(fixedStep);

		if (state == EntityState.WALKING) {
			vel.lerp(Vector2.Zero, 10f * fixedStep);
		} else {
			vel.scl(fixedStep);
		}

		stateTime += fixedStep;
	}

	public void moveWithAccel(Direction dir) {
		if (dir == Direction.UP) {
			accel.y = ACCEL_MAX;
		} else if (dir == Direction.DOWN) {
			accel.y = -ACCEL_MAX;
		} else if (dir == Direction.LEFT) {
			accel.x = -ACCEL_MAX;
		} else if (dir == Direction.RIGHT) {
			accel.x = ACCEL_MAX;
		}
		direction = dir;
		state = EntityState.WALKING;
	}

	protected void tryMove() {
		bounds.y += vel.y;
		fetchCollidableRects();
		for (int i = 0; i < r.length; i++) {
			Rectangle rect = r[i];
			if (bounds.overlaps(rect)) {
				if (vel.y < 0) {
					bounds.y = rect.y + rect.height + 0.01f;
				} else
					bounds.y = rect.y - bounds.height - 0.01f;
				vel.y = 0;
			}
		}

		bounds.x += vel.x;
		fetchCollidableRects();
		for (int i = 0; i < r.length; i++) {
			Rectangle rect = r[i];
			if (bounds.overlaps(rect)) {
				if (vel.x < 0)
					bounds.x = rect.x + rect.width + 0.01f;
				else
					bounds.x = rect.x - bounds.width - 0.01f;
				vel.x = 0;
			}
		}
	}

	private void fetchCollidableRects() {
		int p1x = (int) bounds.x;
		int p1y = (int) Math.floor(bounds.y);
		int p2x = (int) (bounds.x + bounds.width);
		int p2y = (int) Math.floor(bounds.y);
		int p3x = (int) (bounds.x + bounds.width);
		int p3y = (int) (bounds.y + bounds.height);
		int p4x = (int) bounds.x;
		int p4y = (int) (bounds.y + bounds.height);

		try {
			LevelTile tile1 = level.getTiles()[p1x][p1y];
			LevelTile tile2 = level.getTiles()[p2x][p2y];
			LevelTile tile3 = level.getTiles()[p3x][p3y];
			LevelTile tile4 = level.getTiles()[p4x][p4y];

			if (tile1.isCollidable())
				r[0].set(p1x, p1y, 1, 1);
			else
				r[0].set(-1, -1, 0, 0);
			if (tile2.isCollidable())
				r[1].set(p2x, p2y, 1, 1);
			else
				r[1].set(-1, -1, 0, 0);
			if (tile3.isCollidable())
				r[2].set(p3x, p3y, 1, 1);
			else
				r[2].set(-1, -1, 0, 0);
			if (tile4.isCollidable())
				r[3].set(p4x, p4y, 1, 1);
			else
				r[3].set(-1, -1, 0, 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			Gdx.app.log("Creature", "Player went off screen");
		}
	}

	public boolean isNotWalking() {
		if (vel.x > -MIN_WALK_VELOCITY && vel.x < MIN_WALK_VELOCITY
				&& vel.y > -MIN_WALK_VELOCITY && vel.y < MIN_WALK_VELOCITY) {
			state = EntityState.IDLE;
			return true;
		}
		return false;
	}
}