/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.unbreakablepixeldungeon.items.wands;

import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.unbreakablepixeldungeon.Assets;
import com.watabou.unbreakablepixeldungeon.Dungeon;
import com.watabou.unbreakablepixeldungeon.ResultDescriptions;
import com.watabou.unbreakablepixeldungeon.actors.Actor;
import com.watabou.unbreakablepixeldungeon.actors.Char;
import com.watabou.unbreakablepixeldungeon.actors.buffs.Buff;
import com.watabou.unbreakablepixeldungeon.actors.buffs.Paralysis;
import com.watabou.unbreakablepixeldungeon.actors.mobs.Mob;
import com.watabou.unbreakablepixeldungeon.effects.CellEmitter;
import com.watabou.unbreakablepixeldungeon.effects.MagicMissile;
import com.watabou.unbreakablepixeldungeon.effects.Speck;
import com.watabou.unbreakablepixeldungeon.levels.Level;
import com.watabou.unbreakablepixeldungeon.mechanics.Ballistica;
import com.watabou.unbreakablepixeldungeon.scenes.GameScene;
import com.watabou.unbreakablepixeldungeon.utils.BArray;
import com.watabou.unbreakablepixeldungeon.utils.GLog;
import com.watabou.unbreakablepixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class WandOfAvalanche extends Wand {

	{
		name = "Wand of Avalanche";
		hitChars = false;
	}

	@Override
	protected void onZap( int cell ) {

		Sample.INSTANCE.play( Assets.SND_ROCKS );

		int level = power();

		Ballistica.distance = Math.min( Ballistica.distance, 8 + level );

		int size = 1 + level / 3;
		PathFinder.buildDistanceMap( cell, BArray.not( Level.solid, null ), size );

		int shake = 0;
		for (int i=0; i < Level.LENGTH; i++) {

			int d = PathFinder.distance[i];

			if (d < Integer.MAX_VALUE) {

				Char ch = Actor.findChar( i );
				if (ch != null) {

					ch.sprite.flash();
					ch.damage( Random.Int( 2, (4 + size - d) * (level + 1) ), this );
					
					if (ch.isAlive() && Random.Int( level + 2 + d ) < level + 1) {
						Buff.prolong( ch, Paralysis.class, Random.IntRange( 3, 5 ) );
					}
				}

				if (ch != null && ch.isAlive()) {
					if (ch instanceof Mob) {
						Dungeon.level.mobPress( (Mob)ch );
					} else {
						Dungeon.level.press( i, ch );
					}
				} else {
					Dungeon.level.press( i, null );
				}

				if (Dungeon.visible[i]) {
					CellEmitter.get(i).start(Speck.factory(Speck.ROCK), 0.07f, 3 + (size - d));
					if (Level.water[i]) {
						GameScene.ripple( i );
					}
					if (shake < size - d) {
						shake = size - d;
					}
				}
			}

			Camera.main.shake( 3, 0.07f * (3 + shake) );
		}

		if (!curUser.isAlive()) {
			Dungeon.fail( Utils.format( ResultDescriptions.WAND, name, Dungeon.depth ) );
			GLog.n( "You killed yourself with your own Wand of Avalanche..." );
		}
	}

	protected void fx( int cell, Callback callback ) {
		MagicMissile.earth( curUser.sprite.parent, curUser.pos, cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}

	@Override
	public String desc() {
		return
				"When a discharge of this wand hits a wall (or any other solid obstacle) it causes " +
						"an avalanche of stones, damaging and stunning all creatures in the affected area.";
	}
}