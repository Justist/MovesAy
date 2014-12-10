/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package movesay.hci.team8.movesay.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import movesay.hci.team8.movesay.R;

/**
 * SnakeView: implementation of a simple game of Snake
 * 
 * 
 */
public class SnakeView extends TileView {

    /**
     * Current mode of application: READY to run, RUNNING, or you have already
     * lost. static final ints are used instead of an enum for performance
     * reasons.
     */
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;

    /**
     * Current direction the snake is headed.
     */
    private int mDirection = NORTH;
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;

    /**
     * Labels for the drawables that will be loaded into the TileView class
     */
    private static final int RED_STAR = 1;
    private static final int YELLOW_STAR = 2;
    private static final int GREEN_STAR = 3;

    /**
     * mScore: used to track the number of apples captured mMoveDelay: number of
     * milliseconds between snake movements. This will decrease as apples are
     * captured.
     */
    private long mScore = 0;
    private long mMoveDelay = 600;
    /**
     * mLastMove: tracks the absolute time when the snake last moved, and is used
     * to determine if a move should be made based on mMoveDelay.
     */
    private long mLastMove;
    
    /**
     * mStatusText: text shows to the user in some run states
     */
    private TextView mStatusText;

    /**
     * mSnakeTrail: a list of Coordinates that make up the snake's body
     * mAppleList: the secret location of the juicy apples the snake craves.
     */
    //private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
	 private Coordinate character  = new Coordinate(0, 0);
    private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();

    /**
     * Everyone needs a little randomness in their life
     */
    private static final Random RNG = new Random();

    /**
     * Create a simple handler that we can use to cause animation to happen.  We
     * set ourselves as a target and we can use the sleep()
     * function to cause an update/invalidate to occur at a later date.
     */
    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.update();
            SnakeView.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSnakeView();
   }

    public SnakeView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	initSnakeView();
    }

    private void initSnakeView() {
        setFocusable(true);

        Resources r = this.getContext().getResources();
        
        resetTiles(4);
        loadTile(RED_STAR, r.getDrawable(R.drawable.redstar));
        loadTile(YELLOW_STAR, r.getDrawable(R.drawable.yellowstar));
        loadTile(GREEN_STAR, r.getDrawable(R.drawable.greenstar));
    	
    }
    

    private void initNewGame() {
        //mSnakeTrail.clear();
        mAppleList.clear();

        // For now we're just going to load up a short default eastbound snake
        // that's just turned north

        
        character = new Coordinate(15, 15);
        mDirection = NORTH;

        // Two apples to start with
        addRandomApple();
        addRandomApple();

        mMoveDelay = 1200;
        mScore = 0;
    }


    private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
        int count = cvec.size();
        int[] rawArray = new int[count * 2];
        for (int index = 0; index < count; index++) {
            Coordinate c = cvec.get(index);
            rawArray[2 * index] = c.x;
            rawArray[2 * index + 1] = c.y;
        }
        return rawArray;
    }

    public Bundle saveState() {
        Bundle map = new Bundle();

        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
        map.putInt("mDirection", mDirection);
        map.putLong("mMoveDelay", mMoveDelay);
        map.putLong("mScore", mScore);
        map.putInt("characterX", character.x);
		  map.putInt("characterY", character.y);

        return map;
    }

    private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }

    /**
     * Restore game state if our process is being relaunched
     * 
     * @param icicle a Bundle containing the game state
     */
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);

        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mDirection = icicle.getInt("mDirection");
        mMoveDelay = icicle.getLong("mMoveDelay");
        mScore = icicle.getLong("mScore");
        character.x = icicle.getInt("characterX");
		  character.y = icicle.getInt("characterY");
    }

	public void recognizeDirection(String command) {
		Log.d("SnakeView", command);
		//Log.d("SnakeView", command.equals("start") ? "recognized start" : "did not recognize start");
		if(command.equals("up")) {
			mDirection = NORTH;
		} else if(command.equals("down")) {
			mDirection = SOUTH;
		} else if(command.equals("right")) {
			mDirection = EAST;
		} else if(command.equals("left")) {
			mDirection = WEST;
		} else if(command.equals("start")) {
			if (mMode == READY | mMode == LOSE) {
				initNewGame();
				setMode(RUNNING);
				update();
			}else if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
		} else if(command.equals("pause")) {
			if(mMode == RUNNING) setMode(PAUSE);
		}
	}

    public void setTextView(TextView newView) {
        mStatusText = newView;
    }

    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;

        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {
            str = res.getString(R.string.mode_lose_prefix) + mScore
                  + res.getString(R.string.mode_lose_suffix);
        }

        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }

    /**
     * Selects a random location within the garden that is not currently covered
     * by the snake. Currently _could_ go into an infinite loop if the snake
     * currently fills the garden, but we'll leave discovery of this prize to a
     * truly excellent snake-player.
     * 
     */
    private void addRandomApple() {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // Choose a new location for our apple
            int newX = 1 + RNG.nextInt(mXTileCount - 2);
            int newY = 1 + RNG.nextInt(mYTileCount - 2);
            newCoord = new Coordinate(newX, newY);

            // Make sure it's not already under the snake
            boolean collision = false;
            /*int snakelength = mSnakeTrail.size();
            for (int index = 0; index < snakelength; index++) {
                if (mSnakeTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }*/
				if(character.equals(newCoord)) collision = true;
            // if we're here and there's been no collision, then we have
            // a good location for an apple. Otherwise, we'll circle back
            // and try again
            found = !collision;
        }
		 mAppleList.add(newCoord);
    }


    /**
     * Handles the basic update loop, checking to see if we are in the running
     * state, determining if a move should be made, updating the snake's location.
     */
    public void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateSnake();
                updateApples();
                mLastMove = now;
            }
            mRedrawHandler.sleep(mMoveDelay);
        }
    }

    /**
     * Draws some walls.
     * 
     */
    private void updateWalls() {
        for (int x = 0; x < mXTileCount; x++) {
            setTile(GREEN_STAR, x, 0);
            setTile(GREEN_STAR, x, mYTileCount - 1);
        }
        for (int y = 1; y < mYTileCount - 1; y++) {
            setTile(GREEN_STAR, 0, y);
            setTile(GREEN_STAR, mXTileCount - 1, y);
        }
    }

    /**
     * Draws some apples.
     * 
     */
    private void updateApples() {
        for (Coordinate c : mAppleList) {
            setTile(RED_STAR, c.x, c.y);
        }
    }

    /**
     * Figure out which way the snake is going, see if he's run into anything (the
     * walls, himself, or an apple). If he's not going to die, we then add to the
     * front and subtract from the rear in order to simulate motion. If we want to
     * grow him, we don't subtract from the rear.
     * 
     */
    private void updateSnake() {
        switch (mDirection) {
        case EAST: {
			  character = new Coordinate(character.x + 1, character.y);
            break;
        }
        case WEST: {
			  character = new Coordinate(character.x - 1, character.y);
            break;
        }
        case NORTH: {
			  character = new Coordinate(character.x, character.y - 1);
            break;
        }
        case SOUTH: {
			  character = new Coordinate(character.x, character.y + 1);
            break;
        }
        }

        // Collision detection
        // For now we have a 1-square wall around the entire arena
        if ((character.x < 1) || (character.y < 1) || (character.x > mXTileCount - 2)
				|| (character.y > mYTileCount - 2)) {
            setMode(LOSE);
            return;

        }

        /*// Look for collisions with itself
        int snakelength = mSnakeTrail.size();
        for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
            Coordinate c = mSnakeTrail.get(snakeindex);
            if (c.equals(newHead)) {
                setMode(LOSE);
                return;
            }
        }*/

        // Look for apples
        int applecount = mAppleList.size();
        for (int appleindex = 0; appleindex < applecount; appleindex++) {
            Coordinate c = mAppleList.get(appleindex);
            if (c.equals(character)) {
                mAppleList.remove(c);
                addRandomApple();
                
                mScore++;
                mMoveDelay *= 0.9;
            }
        }

		 setTile(YELLOW_STAR, character.x, character.y);
        /*int index = 0;
        for (Coordinate c : mSnakeTrail) {
            if (index == 0) {
                setTile(YELLOW_STAR, c.x, c.y);
            } else {
                setTile(RED_STAR, c.x, c.y);
            }
            index++;
        }*/

    }

    /**
     * Simple class containing two integer values and a comparison function.
     * There's probably something I should use instead, but this was quick and
     * easy to build.
     * 
     */
    private class Coordinate {
        public int x;
        public int y;

        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
        }

        public boolean equals(Coordinate other) {
			  return x == other.x && y == other.y;
		  }

        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }
    
}
