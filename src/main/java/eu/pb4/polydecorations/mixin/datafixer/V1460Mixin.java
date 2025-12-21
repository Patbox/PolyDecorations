package eu.pb4.polydecorations.mixin.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;

@Mixin(V1460.class)
public abstract class V1460Mixin extends Schema {
    @Shadow protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {};

    @Shadow
    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
    }

    public V1460Mixin(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Inject(method = "registerBlockEntities", at = @At("RETURN"))
    private void registerBlockEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
        var map = cir.getReturnValue();

        registerInventory(schema, map, mod("shelf"));
        registerInventory(schema, map, mod("tool_rack"));
        registerInventory(schema, map, mod("trashcan"));
        registerInventory(schema, map, mod("long_flower_pot"));
        registerInventory(schema, map, mod("basket"));
        registerInventory(schema, map, mod("generic_pickable_storage"));


        schema.register(map, mod("sign_post"), (n) -> DSL.optionalFields("upper", DSL.optionalFields("item", References.ITEM_NAME.in(schema)),
                "lower", DSL.optionalFields("item", References.ITEM_NAME.in(schema))));

        schema.register(map, mod("mailbox"), (n) -> DSL.optionalFields("inventory", DSL.list(DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))))));
        schema.register(map, mod("globe"), (n) -> DSL.optionalFields("item", References.ITEM_STACK.in(schema)));
        schema.register(map, mod("display_case"), (n) -> DSL.optionalFields("item", References.ITEM_STACK.in(schema)));

        schema.registerSimple(map, mod("wind_chime"));
    }

    @Inject(method = "registerEntities", at = @At("RETURN"))
    private void registerEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
        var map = cir.getReturnValue();
        schema.register(map, mod("statue"), () -> DSL.allWithRemainder(
                References.ENTITY_EQUIPMENT.in(schema),
                DSL.optionalFields("stack", References.ITEM_STACK.in(schema))));
        registerMob(schema, map, mod("canvas"));
        registerMob(schema, map, mod("seat"));
    }

    @Unique
    private static String mod(String path) {
        return "polydecorations:" + path;
    }
}