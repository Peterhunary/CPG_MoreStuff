package com.feb.moregrid.mixins;

import com.feb.moregrid.component.additions.guages.DoubleVoltageGaugeComponent;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.patryk3211.powergrid.circuits.components.GaugeComponent;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.electricity.info.customdisplay.Expression;
import org.patryk3211.powergrid.utility.Lang;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(GaugeComponent.class)
public abstract class GaugeComponentMixin {

    @Shadow
    public abstract float getValue(PlacedComponent placed);

    @Shadow
    public abstract float getMaxValue(PlacedComponent placed);

    @Shadow
    public abstract String getUnit(PlacedComponent placed);

    @Shadow
    public abstract ChatFormatting getColor(float value, float maxValue);

    /**
     * @author peterhun
     * @reason Dynamic expression evaluation
     */
    @Overwrite
    public boolean addToGoggleTooltip(
            PlacedComponent component,
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        if (component.has(GaugeComponent.LABEL)) {
            String label = component.get(GaugeComponent.LABEL);

            if (label.isEmpty()) {
                Lang.builder("create")
                        .translate("gui.gauge.info_header")
                        .forGoggles(tooltip);
            } else {
                Lang.text(label).forGoggles(tooltip);
            }
        } else {
            Lang.builder("create")
                    .translate("gui.gauge.info_header")
                    .forGoggles(tooltip);
        }

        Object customData = component.customData;

        if (customData instanceof GaugeComponent.RenderData data) {
            Expression expr =
                    ((RenderDataAccessorMixin) data).moreGrid$getExpression();

            if (expr != null) {
                float[] values = moreGrid$getValues(component);
                float maxValue = getMaxValue(component);

                StringBuilder line = new StringBuilder();
                boolean belowMin = false;
                boolean aboveMax = false;

                for (int i = 0; i < values.length; i++) {
                    if (values[i] < -maxValue) {
                        belowMin = true;
                    }

                    if (values[i] > maxValue) {
                        aboveMax = true;
                    }

                    values[i] = Math.clamp(values[i],
                            -maxValue, maxValue);
                }

                if (belowMin) {
                    line.append("< ");
                } else if (aboveMax) {
                    line.append("> ");
                }

                float evaluatedValue = moreGrid$eval(expr, values);

                if (evaluatedValue >= 0.0F) {
                    line.append(" ");
                }

                line.append(String.format("%.2f ", evaluatedValue));

                LangBuilder textComponent = Lang.text(line.toString())
                        .style(getColor(moreGrid$maxAbs(values), maxValue));

                textComponent.add(Component.literal(getUnit(component)));
                textComponent.forGoggles(tooltip);

                return true;
            }
        }

        Lang.translate("gui.invalid_equation")
                .style(ChatFormatting.RED)
                .forGoggles(tooltip);

        return true;
    }


    @Unique
    private float[] moreGrid$getValues(PlacedComponent component) {
        if (component.component instanceof DoubleVoltageGaugeComponent gauge) {
            return gauge.moreGrid$getValues(component);
        }

        return new float[]{getValue(component)};
    }

    @Unique
    private static float moreGrid$maxAbs(float[] values) {
        float max = 0.0F;

        for (float value : values) {
            max = Math.max(max, Math.abs(value));
        }

        return max;
    }
    @Unique
    private float moreGrid$eval(Expression expr, float[] values) {
        return expr.eval(name -> switch (name) {
            case "x" -> values.length > 0 ? values[0] : 0.0F;
            case "y" -> values.length > 1 ? values[1] : 0.0F;
            case "z" -> values.length > 2 ? values[2] : 0.0F;
            default -> 0.0F;
        });
    }
}