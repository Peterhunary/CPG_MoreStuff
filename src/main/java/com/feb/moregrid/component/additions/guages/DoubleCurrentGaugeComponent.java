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

import com.feb.moregrid.MoreGrid;
import com.google.common.collect.ImmutableCollection;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.GaugeComponent;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.components.properties.FloatProperty;
import org.patryk3211.powergrid.circuits.components.properties.StringProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.patryk3211.powergrid.electricity.gauge.CurrentGaugeBlockEntity;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;


public abstract class DoubleCurrentGaugeComponent extends GaugeComponent {

    public static final FloatProperty MAX_CURRENT1 =
            new FloatProperty(MoreGrid.MOD_ID, "current_gauge_max1", 0.1f, 0.01f, 10.0f);

    public static final FloatProperty MAX_CURRENT2 =
            new FloatProperty(MoreGrid.MOD_ID, "current_gauge_max2", 0.1f, 0.01f, 10.0f);

    public static final StringProperty UNIT =
            new StringProperty(MoreGrid.MOD_ID, "gauge_unit", "A");

    public DoubleCurrentGaugeComponent(ComponentFootprint footprint) {
        super(footprint);
    }

    @Override
    protected void addProperties(
            ImmutableCollection.Builder<ComponentProperty<?>> properties
    ) {
        super.addProperties(properties);
        properties.add(UNIT, MAX_CURRENT1, MAX_CURRENT2);
    }

    @Override
    public void bake(
            @NotNull PlacedComponent placed,
            @NotNull ComponentCircuitBuilder builder,
            ThermalBuilder.@NotNull IEmitter thermals
    ) {
        ElectricWire primary = builder.connect(
                0.05f,
                builder.terminalNode(0),
                builder.terminalNode(1)
        );

        ElectricWire secondary = builder.connect(
                0.05f,
                builder.terminalNode(2),
                builder.terminalNode(3)
        );

        placed.add(primary);
        placed.add(secondary);
    }

    public float[] moreGrid$getValues(PlacedComponent component) {
        if (component.wires.size() < 2) {
            return new float[]{0.0f, 0.0f};
        }

        ElectricWire primary = (ElectricWire) component.wires.get(0);
        ElectricWire secondary = (ElectricWire) component.wires.get(1);

        return new float[]{
                (float) primary.current(),
                (float) secondary.current()
        };
    }

    @Override
    public float getTarget(PlacedComponent placed) {
        float[] values = moreGrid$getValues(placed);

        float current1 = Math.abs(values[0]);
        float current2 = Math.abs(values[1]);

        float max1 = placed.get(MAX_CURRENT1);
        float max2 = placed.get(MAX_CURRENT2);

        float totalCurrent = current1 + current2;
        float totalMax = max1 + max2;

        return Mth.clamp(totalCurrent / totalMax, 0.0f, 1.125f);
    }

    @Override
    public float getMaxValue(PlacedComponent placed) {
        return placed.get(MAX_CURRENT1) + placed.get(MAX_CURRENT2);
    }

    @Override
    public String getUnit(PlacedComponent placed) {
        return placed.getString(UNIT);
    }

    @Override
    public ChatFormatting getColor(float value, float maxValue) {
        return CurrentGaugeBlockEntity.measurementColor(value, maxValue);
    }
}