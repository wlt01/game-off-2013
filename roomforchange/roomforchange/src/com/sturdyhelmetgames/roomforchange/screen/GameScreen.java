package com.sturdyhelmetgames.roomforchange.screen;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.sturdyhelmetgames.roomforchange.RoomForChangeGame;
import com.sturdyhelmetgames.roomforchange.assets.Assets;
import com.sturdyhelmetgames.roomforchange.entity.Entity.Direction;
import com.sturdyhelmetgames.roomforchange.level.Level;
import com.sturdyhelmetgames.roomforchange.tween.Vector3Accessor;
import com.sturdyhelmetgames.roomforchange.util.LabyrinthUtil;

public class GameScreen extends Basic2DScreen {

	public static final int SCALE = 10;
	private OrthographicCamera cameraMiniMap;
	private SpriteBatch batchMiniMap;
	public ScreenQuake screenQuake;
	public final Vector2 currentCamPosition = new Vector2();
	public final TweenManager cameraTweenManager = new TweenManager();

	private Level level;

	public GameScreen() {
		super();
		screenQuake = new ScreenQuake(camera);
	}

	public GameScreen(RoomForChangeGame game) {
		super(game, 12, 8);
		screenQuake = new ScreenQuake(camera);

		camera.position.set(6f, 4f, 0f);
		camera.update();

		// setup our mini map camera
		cameraMiniMap = new OrthographicCamera(12, 8);
		cameraMiniMap.zoom = SCALE;
		cameraMiniMap.position.set(0f, 0f, 0f);
		cameraMiniMap.update();
		batchMiniMap = new SpriteBatch();

		level = LabyrinthUtil.generateLabyrinth(4, 4, this);
	}

	@Override
	protected void updateScreen(float fixedStep) {
		processKeys();

		level.update(fixedStep);

		final Vector2 currentPiecePos = level
				.findCurrentPieceRelativeToMapPosition();
		if (!currentPiecePos.epsilonEquals(currentCamPosition, 0.1f)) {
			Tween.to(camera.position, Vector3Accessor.POSITION_XY, 0.7f)
					.target(currentPiecePos.x + 6f, currentPiecePos.y + 4f, 0f)
					.ease(Quad.INOUT).start(cameraTweenManager);
			currentCamPosition.set(currentPiecePos);
		}
		camera.update();

		screenQuake.update(fixedStep);
		cameraTweenManager.update(fixedStep);
	}

	@Override
	public void renderScreen(float delta) {
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
		level.render(delta, spriteBatch);
		spriteBatch.end();

		batchMiniMap.setProjectionMatrix(cameraMiniMap.combined);
		batchMiniMap.begin();
		level.render(delta, batchMiniMap);
		batchMiniMap.end();
	}

	protected void processKeys() {
		if (!paused) {
			// process player movement keys
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				level.player.moveWithAccel(Direction.UP);
			}
			if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				level.player.moveWithAccel(Direction.DOWN);
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				level.player.moveWithAccel(Direction.RIGHT);
			}
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				level.player.moveWithAccel(Direction.LEFT);
			}

			if (Gdx.input.isKeyPressed(Keys.MINUS)) {
				camera.zoom += 0.1f;
				camera.update();
			}
			if (Gdx.input.isKeyPressed(Keys.PLUS)) {
				camera.zoom -= 0.1f;
				camera.update();
			}
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.W) {
			startScreenQuake(Level.UP);
			return true;
		}
		if (keycode == Keys.S) {
			startScreenQuake(Level.DOWN);
			return true;
		}
		if (keycode == Keys.A) {
			startScreenQuake(Level.LEFT);
			return true;
		}
		if (keycode == Keys.D) {
			startScreenQuake(Level.RIGHT);
			return true;
		}
		return super.keyDown(keycode);
	}

	@Override
	public void dispose() {
		super.dispose();
		batchMiniMap.dispose();
	}

	public void updateCameraPos(float xOffset, float yOffset) {
		camera.position.set(camera.position.x + xOffset, camera.position.y
				+ yOffset, 0f);
		camera.update();
	}

	public void startScreenQuake(final int dir) {
		screenQuake.activate(2.8f, new Runnable() {
			@Override
			public void run() {
				level.resumeEntities();
				level.moveLabyrinthPiece(dir);
			}
		});
		Assets.getGameSound(Assets.SOUND_STONEDOOR).play(0.5f, 1.5f, 0f);
		level.pauseEntities();
	}
}