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
 * CharacterView: implementation of a simple game of Character
 * 
 * 
 */
public class CharacterView extends TileView {

    /**
     * Current mode of application: READY to run, RUNNING, or you have already
     * lost. static final ints are used instead of an enum for performance
     * reasons.
     */
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    private static final int RUNNING = 2;
    private static final int LOSE = 3;

	public static enum Sprites {
		RED_STAR(1), GREEN_STAR(2), ARROW_UP(3), ARROW_DOWN(4), ARROW_LEFT(5), ARROW_RIGHT(6);
	 	private int sprite;
		private Sprites(int s) {sprite = s;}
		public int getValue() {return sprite;}
	}

	private int mDirection = Sprites.ARROW_UP.getValue();
	/**
     * mScore: used to track the number of apples captured mMoveDelay: number of
     * milliseconds between Character movements. This will decrease as apples are
     * captured.
     */
    private long mScore = 0;
    private long mMoveDelay = 600;
    /**
     * mLastMove: tracks the absolute time when the Character last moved, and is used
     * to determine if a move should be made based on mMoveDelay.
     */
    private long mLastMove;
    
    /**
     * mStatusText: text shows to the user in some run states
     */
    private TextView mStatusText;

	private int stepsToWalk;

    /**
     * mCharacterTrail: a list of Coordinates that make up the Character's body
     * mGoalList: the secret location of the juicy apples the Character craves.
     */
    //private ArrayList<Coordinate> mCharacterTrail = new ArrayList<Coordinate>();
	 private Coordinate character  = new Coordinate(0, 0);
    private ArrayList<Coordinate> mGoalList = new ArrayList<Coordinate>();

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
            CharacterView.this.update();
            CharacterView.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    public CharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCharacterView();
   }

    public CharacterView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	initCharacterView();
    }

	private void initCharacterView() {
		setFocusable(true);

		Resources r = this.getContext().getResources();

		resetTiles(Sprites.values().length + 1);
		loadTile(Sprites.RED_STAR, R.drawable.redstar);
		loadTile(Sprites.GREEN_STAR, R.drawable.greenstar);
		loadTile(Sprites.ARROW_UP, R.drawable.arrowup);
		loadTile(Sprites.ARROW_DOWN, R.drawable.arrowdown);
		loadTile(Sprites.ARROW_LEFT, R.drawable.arrowleft);
		loadTile(Sprites.ARROW_RIGHT, R.drawable.arrowright);
	}
    

    private void initNewGame() {
       mGoalList.clear();
		 stepsToWalk = 0;

        
        character = new Coordinate(15, 15);
        mDirection = Sprites.ARROW_UP.getValue();
		 Log.d("initNewGame", String.valueOf(mDirection));
		  setTile(mDirection, character.x, character.y);

        addRandomGoal();
        addRandomGoal();

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

        map.putIntArray("mGoalList", coordArrayListToArray(mGoalList));
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

        mGoalList = coordArrayToArrayList(icicle.getIntArray("mGoalList"));
        mDirection = icicle.getInt("mDirection");
        mMoveDelay = icicle.getLong("mMoveDelay");
        mScore = icicle.getLong("mScore");
        character.x = icicle.getInt("characterX");
		  character.y = icicle.getInt("characterY");
    }

	public void recognizeDirection(String command) {
		if(command.isEmpty()) {return;}
		if(command.equals("up")) {
			mDirection = Sprites.ARROW_UP.getValue();
			setTile(mDirection, character.x, character.y);
		} else if(command.equals("down")) {
			mDirection = Sprites.ARROW_DOWN.getValue();
			setTile(mDirection, character.x, character.y);
		} else if(command.equals("right")) {
			mDirection = Sprites.ARROW_RIGHT.getValue();
			setTile(mDirection, character.x, character.y);
		} else if(command.equals("left")) {
			mDirection = Sprites.ARROW_LEFT.getValue();
			setTile(mDirection, character.x, character.y);
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
		} else if(String.valueOf(command.charAt(0)).equals("N")) {
			try{stepsToWalk = Integer.parseInt(command.substring(1, command.length()));}
			catch(NumberFormatException n) {Log.d(TAG, String.valueOf(n));}
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
     * by the Character. Currently _could_ go into an infinite loop if the Character
     * currently fills the garden, but we'll leave discovery of this prize to a
     * truly excellent Character-player.
     * 
     */
    private void addRandomGoal() {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // Choose a new location for our apple
            int newX = 1 + RNG.nextInt(mXTileCount - 2);
            int newY = 1 + RNG.nextInt(mYTileCount - 2);
            newCoord = new Coordinate(newX, newY);

            // Make sure it's not already under the Character
            boolean collision = false;
            /*int Characterlength = mCharacterTrail.size();
            for (int index = 0; index < Characterlength; index++) {
                if (mCharacterTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }*/
				if(character.equals(newCoord)) collision = true;
            // if we're here and there's been no collision, then we have
            // a good location for an apple. Otherwise, we'll circle back
            // and try again
            found = !collision;
        }
		 mGoalList.add(newCoord);
    }


    /**
     * Handles the basic update loop, checking to see if we are in the running
     * state, determining if a move should be made, updating the Character's location.
     */
	 void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateCharacter();
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
            setTile(Sprites.GREEN_STAR.getValue(), x, 0);
            setTile(Sprites.GREEN_STAR.getValue(), x, mYTileCount - 1);
        }
        for (int y = 1; y < mYTileCount - 1; y++) {
            setTile(Sprites.GREEN_STAR.getValue(), 0, y);
            setTile(Sprites.GREEN_STAR.getValue(), mXTileCount - 1, y);
        }
    }

    /**
     * Draws some apples.
     * 
     */
    private void updateApples() {
		 //Log.d("Applelist", mGoalList.toString());
        for (Coordinate c : mGoalList) {
            setTile(Sprites.RED_STAR.getValue(), c.x, c.y);
        }
    }

    /**
     * Figure out which way the Character is going, see if he's run into anything (the
     * walls, himself, or an apple). If he's not going to die, we then add to the
     * front and subtract from the rear in order to simulate motion. If we want to
     * grow him, we don't subtract from the rear.
     * 
     */
    private void updateCharacter() {
		 int oldX = character.x, oldY = character.y;
		 //Walk
		 if(stepsToWalk > 0) {
			 if(mDirection == Sprites.ARROW_UP.getValue()) {
				 character.y--;
			 }
			 else if(mDirection == Sprites.ARROW_DOWN.getValue()) {
				 character.y++;
			 }
			 else if(mDirection == Sprites.ARROW_LEFT.getValue()) {
				 character.x--;
			 }
			 else if(mDirection == Sprites.ARROW_RIGHT.getValue()) {
				 character.x++;
			 }
			 stepsToWalk--;
		 }
        // Collision detection with wall
        if ((character.x < 1) || (character.y < 1) || (character.x > mXTileCount - 2)
				|| (character.y > mYTileCount - 2)) {
			  stepsToWalk = 0;
			  character.x = oldX;
			  character.y = oldY;
           return;
        }

        // Look for goals
        int goalCount = mGoalList.size();
        for (int i = 0; i < goalCount; i++) {
            Coordinate c = mGoalList.get(i);
            if (c.equals(character)) {
                mGoalList.remove(c);
                addRandomGoal();
                
                mScore++;
                mMoveDelay *= 0.9;
            }
        }

		 setTile(mDirection, character.x, character.y);
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
