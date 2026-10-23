package com.feb.moregrid.mixins;

import org.patryk3211.powergrid.circuits.components.GaugeComponent;
import org.patryk3211.powergrid.electricity.info.customdisplay.Expression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GaugeComponent.RenderData.class)
public interface RenderDataAccessorMixin {

    @Accessor("expression")
    Expression moreGrid$getExpression();
}
