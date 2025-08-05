package com;

import arc.*;
import arc.util.*;
import com.blocks.factory.KSImpactor;
import com.blocks.factory.sublimation;
import com.items.items;
import com.liquids.liquids;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

public class main extends Mod{

    public main(){
        Log.info("Loaded ExampleJavaMod constructor.");

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("behold").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                dialog.cont.button("I see", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
        items.load();
        liquids.load();

        new sublimation("sublimation"){{
            localizedName = "升华器";
            description = "蓄能完毕后，产生诅咒黄金————咒金";
        }};

        new KSImpactor("KSImpactor"){{
            localizedName = "KS冲击器";
            description = "把KS1688457强行冲进‘升化器’内";
            details = "这种鲁莽的手段看起来非常不可靠，但是据百年来的炼金史来看......他应该不会产生什么不可逆的后果。\n但愿吧";
        }};
    }

}
