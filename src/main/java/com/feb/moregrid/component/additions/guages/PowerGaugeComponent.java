/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feb.moregrid.component.additions.guages;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.GaugeComponent;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.components.properties.FloatProperty;
import org.patryk3211.powergrid.circuits.components.properties.StringProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.patryk3211.powergrid.electricity.gauge.PowerGaugeBlockEntity;

public class PowerGaugeComponent extends GaugeComponent {
    public static final FloatProperty MAX_POWER = new FloatProperty(PowerGrid.MOD_ID, "power_gauge_max", 100.0f, 1.0f, 50000.0f);
    public static final StringProperty UNIT = new StringProperty(PowerGrid.MOD_ID, "gauge_unit", "W");

    public PowerGaugeComponent(ComponentFootprint footprint) {
        super(footprint);
    }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(UNIT, MAX_POWER);
    }

    @Override
    public void bake(@NotNull PlacedComponent placed, @NotNull ComponentCircuitBuilder builder, ThermalBuilder.@NotNull IEmitter thermals) {
        var wire = builder.connect(100_000f, builder.terminalNode(0), builder.terminalNode(1));
        placed.add(wire);
    }

    @Override
    public float getTarget(PlacedComponent placed) {
        if(placed.wires.isEmpty())
            return 0;
        var wire = placed.wires.getFirst();
        return Mth.clamp((float) (Math.abs(wire.internalPower()) / placed.get(MAX_POWER)), 0, 1.125f);
    }

    @Override
    public float getValue(PlacedComponent placed) {
        if(placed.wires.isEmpty())
            return 0;
        var wire = placed.wires.getFirst();
        return Math.abs((float) wire.internalPower());
    }

    @Override
    public float getMaxValue(PlacedComponent placed) {
        return placed.get(MAX_POWER);
    }

    @Override
    public String getUnit(PlacedComponent placed) {
        return placed.get(UNIT);
    }

    @Override
    public ChatFormatting getColor(float value, float maxValue) {
        return PowerGaugeBlockEntity.measurementColor(value, maxValue);
    }
}
