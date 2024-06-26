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
package com.watabou.unbreakablepixeldungeon.actors.buffs;

import com.watabou.unbreakablepixeldungeon.actors.Char;
import com.watabou.unbreakablepixeldungeon.actors.hero.HeroClass;
import com.watabou.unbreakablepixeldungeon.effects.CellEmitter;
import com.watabou.unbreakablepixeldungeon.effects.Speck;
import com.watabou.unbreakablepixeldungeon.sprites.HeroSprite;
import com.watabou.unbreakablepixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Disguise extends FlavourBuff {
    
    public static final float DURATION = 100f;
    
    public HeroClass costume;
    
    private static final String COSTUME = "costume";
    
    public void choose( HeroClass notHeroClass, HeroClass notCostume ) {
        HeroClass[] costumes = { HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE, HeroClass.HUNTRESS };
        while (true) {
            costume = Random.element( costumes );
            if (costume != notHeroClass && costume != notCostume) {
                return;
            }
        }
    }
    
    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( COSTUME, costume );
    }
    
    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        costume = bundle.getEnum( COSTUME, HeroClass.class );
    }
    
    @Override
    public boolean attachTo( Char target ) {
        if (super.attachTo( target )) {
            if (target.sprite != null) {
                CellEmitter.get( target.pos ).burst( Speck.factory( Speck.WOOL ), 6 );
            }
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void detach() {
        CellEmitter.get( target.pos ).burst( Speck.factory( Speck.WOOL ), 6 );
        super.detach();
        if (target.sprite != null) {
            ((HeroSprite)target.sprite).updateTexture();
        }
    }
    
    @Override
    public int icon() {
        return BuffIndicator.DISGUISE;
    }
    
    public String toString() {
        switch (costume) {
            case HUNTRESS:
                return "Disguised as a huntress";
            case MAGE:
                return "Disguised as a mage";
            case ROGUE:
                return "Disguised as a rogue";
            case WARRIOR:
                return "Disguised as a warrior";
            default:
                return "";
        }
    }
}
