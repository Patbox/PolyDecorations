package eu.pb4.polydecorations.mixin.datafixer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.util.datafix.schemas.V3818_3;

@Mixin(V3818_3.class)
public class V3818_3Mixin {
    /*@ModifyArg(method = "method_57277", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/DSL;optionalFields([Lcom/mojang/datafixers/util/Pair;)Lcom/mojang/datafixers/types/templates/TypeTemplate;"))
    private static Pair<String, TypeTemplate>[] addCustomComponents(Pair<String, TypeTemplate>[] components,
                                                                    @Local(argsOnly = true) Schema schema) {
        var list = new ArrayList<>(List.of(components));

        list.add(Pair.of("brewery:cooking_data", DSL.optionalFields(
                "ingredients", DSL.list(TypeReferences.ITEM_STACK.in(schema)),
                "heat_source", TypeReferences.BLOCK_NAME.in(schema)
                )));

        return list.toArray(components);
    }*/
}
