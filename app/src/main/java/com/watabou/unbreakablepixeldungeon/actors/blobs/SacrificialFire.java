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
package com.watabou.unbreakablepixeldungeon.actors.blobs;

import com.watabou.noosa.audio.Sample;
import com.watabou.unbreakablepixeldungeon.Assets;
import com.watabou.unbreakablepixeldungeon.Dungeon;
import com.watabou.unbreakablepixeldungeon.DungeonTilemap;
import com.watabou.unbreakablepixeldungeon.Journal;
import com.watabou.unbreakablepixeldungeon.Journal.Feature;
import com.watabou.unbreakablepixeldungeon.actors.Actor;
import com.watabou.unbreakablepixeldungeon.actors.Char;
import com.watabou.unbreakablepixeldungeon.actors.buffs.Buff;
import com.watabou.unbreakablepixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.unbreakablepixeldungeon.actors.hero.Hero;
import com.watabou.unbreakablepixeldungeon.actors.mobs.Mob;
import com.watabou.unbreakablepixeldungeon.effects.BlobEmitter;
import com.watabou.unbreakablepixeldungeon.effects.Flare;
import com.watabou.unbreakablepixeldungeon.effects.Wound;
import com.watabou.unbreakablepixeldungeon.effects.particles.SacrificialParticle;
import com.watabou.unbreakablepixeldungeon.items.scrolls.ScrollOfWipeOut;
import com.watabou.unbreakablepixeldungeon.scenes.GameScene;
import com.watabou.unbreakablepixeldungeon.ui.BuffIndicator;
import com.watabou.unbreakablepixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class SacrificialFire extends Blob {
	
	private static final String TXT_WORTHY		= "\"Your sacrifice is worthy...\" ";
	private static final String TXT_UNWORTHY	= "\"Your sacrifice is unworthy...\" ";
	private static final String TXT_REWARD		= "\"Your sacrifice is worthy and so you are!\" ";
	
	protected int pos;
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		
		for (int i=0; i < LENGTH; i++) {
			if (cur[i] > 0) {
				pos = i;
				break;
			}
		}
	}
	
	@Override
	protected void evolve() {
		volume = off[pos] = cur[pos];
		Char ch = Actor.findChar( pos );
		if (ch != null) {
			if (Dungeon.visible[pos] && ch.buff( Marked.class ) == null) {
				ch.sprite.emitter().burst( SacrificialParticle.FACTORY, 20 );
				Sample.INSTANCE.play( Assets.SND_BURNING );
			}
			Buff.prolong( ch, Marked.class, Marked.DURATION );
			
			for (Mob mob : Dungeon.level.mobs) {
				if (mob != ch) {
					mob.beckon( pos );
				}
			}
		}
		if (Dungeon.visible[pos]) {
			Journal.add( Feature.SACRIFICIAL_FIRE );
		}
	}
	
	@Override
	public void seed( int cell, int amount ) {
		cur[pos] = 0;
		pos = cell;
		volume = cur[pos] = amount;
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		
		emitter.pour( SacrificialParticle.FACTORY, 0.04f );
	}
	
	public static void sacrifice( Char ch ) {
		
		Wound.hit( ch );
		
		SacrificialFire fire = (SacrificialFire)Dungeon.level.blobs.get( SacrificialFire.class );
		if (fire != null) {
			
			int exp = 0;
			if (ch instanceof Mob) {
				exp = ((Mob)ch).exp() * Random.IntRange( 1, 3 );
			} else if (ch instanceof Hero) {
				exp = ((Hero)ch).maxExp();
			}
			
			if (exp > 0) {
				
				int volume = fire.volume - exp;
				if (volume > 0) {
					fire.seed( fire.pos, volume );
					GLog.w( TXT_WORTHY );
				} else {
					fire.seed( fire.pos, 0 );
					Journal.remove( Feature.SACRIFICIAL_FIRE );
					
					GLog.w( TXT_REWARD );
					GameScene.effect( new Flare( 7, 32 ).color( 0x66FFFF, true ).show( ch.sprite.parent, DungeonTilemap.tileCenterToWorld( fire.pos ), 2f ) );
					Dungeon.level.drop( new ScrollOfWipeOut(), fire.pos ).sprite.drop();
				}
			} else {
				
				GLog.w( TXT_UNWORTHY );
				
			}
		}
	}
	
	@Override
	public String tileDesc() {
		return "Sacrificial fire burns here. Every creature touched by this fire is marked as an offering for the spirits of the dungeon.";
	}
	
	public static class Marked extends FlavourBuff {

		public static final float DURATION	= 5f;
		
		@Override
		public int icon() {
			return BuffIndicator.SACRIFICE;
		}
		
		@Override
		public String toString() {
			return "Marked for sacrifice";
		}
		
		@Override
		public void detach() {
			if (!target.isAlive()) {
				sacrifice( target );
			}
			super.detach();
		}
	}

}
