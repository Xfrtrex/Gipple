package net.giplets.gipple.gipple.init;

import net.minecraft.item.FoodComponent;

public class GippleFoodComponents {
    public static final FoodComponent GELATIN = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.3F).snack().build();
    public static final FoodComponent GELATIN_SOUP = (new FoodComponent.Builder()).hunger(9).saturationModifier(0.6F).build();

}
