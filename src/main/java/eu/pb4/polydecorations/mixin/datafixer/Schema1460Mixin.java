package eu.pb4.polydecorations.mixin.datafixer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.Schema100;
import net.minecraft.datafixer.schema.Schema1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(Schema1460.class)
public abstract class Schema1460Mixin extends Schema {
    @Shadow protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {};

    @Shadow
    protected static void targetEntityItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
    }

    public Schema1460Mixin(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Inject(method = "registerBlockEntities", at = @At("RETURN"))
    private void registerBlockEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
        var map = cir.getReturnValue();

        registerInventory(schema, map, mod("shelf"));
        registerInventory(schema, map, mod("tool_rack"));
        registerInventory(schema, map, mod("trashcan"));


        schema.register(map, mod("sign_post"), (n) -> DSL.optionalFields("upper", DSL.optionalFields("item", TypeReferences.ITEM_NAME.in(schema)),
                "lower", DSL.optionalFields("item", TypeReferences.ITEM_NAME.in(schema))));

        schema.register(map, mod("mailbox"), (n) -> DSL.optionalFields("inventory", DSL.list(DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema))))));
        schema.register(map, mod("globe"), (n) -> DSL.optionalFields("item", TypeReferences.ITEM_STACK.in(schema)));
        schema.register(map, mod("display_case"), (n) -> DSL.optionalFields("item", TypeReferences.ITEM_STACK.in(schema)));
    }

    @Inject(method = "registerEntities", at = @At("RETURN"))
    private void registerEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
        var map = cir.getReturnValue();
        schema.register(map, mod("statue"), () -> DSL.allWithRemainder(
                TypeReferences.ENTITY_EQUIPMENT.in(schema),
                DSL.optionalFields("stack", TypeReferences.ITEM_STACK.in(schema))));
        targetEntityItems(schema, map, mod("canvas"));
        targetEntityItems(schema, map, mod("seat"));
    }

    @Unique
    private static String mod(String path) {
        return "polydecorations:" + path;
    }
}