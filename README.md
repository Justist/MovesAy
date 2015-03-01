MovesAy is an Android game which revolves around the ability to walk around and play the game solely by using voice commands.
Requires at least Android 4.0 and is built in [Android Studio](https://developer.android.com/sdk/index.html).

How it works:
=============

Game starts by saying 'Start', pauses by saying 'Pause', and quits by saying 'Quit'.
Say 'Up', 'Left', 'Down', or 'Right' to let the character face in a direction. 
Say 'Walk' followed by a tiny pause and then a number of steps (for example 'Nine') to walk that number of steps 
in the current direction.
Make sure to walk over the red dots to increase your score, which you can see if you pause the game.

Current progress:
=================

- Can interpret most commands correctly in a silent environment.
- Character can walk around and pick up red dots ('goals'), which increase it's score.
- Game can start, pause, and quit.

To be implemented:
==================

- Graphical enhancement (better sprites and such).
- More options for command: Not only saying 'Walk' followed by a number, but also an option to move 1 step in a direction
when saying that direction (so 'Up' let's the character set 1 step to above). Preferrably switchable in the pause menu.
- More gameplay: Not only picking up dots to increase score, but actual puzzels and such. Slaying monsters probably does
not work well due to the delay of the speech recognition.
- Better understanding of the commands, as it is now for roughly 80% accurate in a silent environment 
(for about 2% in a noisy environment). Numbers are generally misheard.

Bugs:
=====

- Can crash when having too many voice commands in a short period of time.
- Can keep listening with slight noise in the background. Needs timeout.
- Mishears quite a lot of commands (see also in 'To be implemented').

MovesAy is licensed under the Apache 2.0 license. See [the terms and
conditions](http://www.apache.org/licenses/LICENSE-2.0) for more details.
